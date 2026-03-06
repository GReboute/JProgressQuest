package com.JProgressQuest.service;

import com.JProgressQuest.model.Constants;
import com.JProgressQuest.model.Game;
import com.JProgressQuest.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Service principal gérant la logique du jeu Progress Quest.
 * Utilise les fonctionnalités modernes de Java 17 et les patterns de programmation réactive.
 */
public class GameService {
    
    // Logger SLF4J (meilleure pratique que System.out.println)
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    
    private final RandomService randomService;
    private final NameGenerator nameGenerator;
    private final StorageService storageService;
    
    // ScheduledExecutorService (Java 5+) pour la gestion des tâches temporisées
    private final ScheduledExecutorService scheduler;
    
    // État actuel du jeu
    private Game currentGame;
    private boolean gameRunning = false;
    
    // Gestionnaire d'événements - Interface fonctionnelle (Java 8+)
    @FunctionalInterface
    public interface GameEventListener {
        void onGameEvent(Constants.GameEvent event);
    }
    
    private GameEventListener eventListener;
    
    /**
     * Constructeur avec injection de dépendances
     */
    public GameService(RandomService randomService, NameGenerator nameGenerator, StorageService storageService) {
        this.randomService = randomService;
        this.nameGenerator = nameGenerator;
        this.storageService = storageService;
        
        // ThreadPool avec un seul thread pour les tâches de jeu
        this.scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "GameService-Timer");
            t.setDaemon(true); // Thread daemon pour ne pas bloquer l'arrêt de l'application
            return t;
        });
    }
    
    /**
     * Constructeur par défaut
     */
    public GameService() {
        this(new RandomService(), new NameGenerator(), new StorageService());
    }
    
    /**
     * Crée un nouveau personnage
     */
    public Game createNewCharacter(String name, String race, String characterClass) {
        logger.info("Création d'un nouveau personnage: {}", name);
        
        Game game = new Game(name);
        
        // Configuration des traits de base
        game.setTrait("Name", name);
        game.setTrait("Race", race);
        game.setTrait("Class", characterClass);
        game.setTrait("Level", 1);
        
        // Génération des statistiques de base
        generateBaseStats(game);
        
        // Équipement de départ
        equipStartingGear(game);
        
        // Queue de tâches initiale - Text Block (Java 15+) pour plus de lisibilité
        String initialQuest = """
            Experiencing an enigmatic and foreboding night vision
            """.trim();
        
        game.getTaskBar().reset(10000); // 10 secondes
        game.setCurrentTask(initialQuest);
        
        // Initialisation des barres de progression
        initializeProgressBars(game);
        
        return game;
    }
    
    /**
     * Génère les statistiques de base du personnage
     */
    private void generateBaseStats(Game game) {
        // Génération des stats primaires avec 3d6 (comme dans D&D)
        Constants.PRIME_STATS.forEach(stat -> {
            int value = rollStat();
            game.addToStat(stat, value - 10); // Base 10, ajout de la différence
        });
        
        // Calcul des stats dérivées
        int constitution = game.getStat("CON");
        int intelligence = game.getStat("INT");
        
        game.addToStat("HP Max", randomService.random(8) + constitution / 6);
        game.addToStat("MP Max", randomService.random(8) + intelligence / 6);
        
        logger.debug("Stats générées pour {}: STR={}, CON={}, etc.", 
                    game.getTrait("Name"), game.getStat("STR"), constitution);
    }
    
    /**
     * Lance 3d6 pour une statistique
     */
    private int rollStat() {
        return 3 + randomService.random(6) + randomService.random(6) + randomService.random(6);
    }
    
    /**
     * Équipe le personnage avec l'équipement de départ
     */
    private void equipStartingGear(Game game) {
        game.equipItem("Weapon", "Rusty Dagger");
        game.equipItem("Hauberk", "-3 Burlap");
        
        // Ajout d'or de départ
        game.addToInventory("Gold", 50);
    }
    
    /**
     * Initialise les barres de progression
     */
    private void initializeProgressBars(Game game) {
        int level = (Integer) game.getTrait("Level");
        
        game.getExpBar().reset(calculateLevelUpTime(level));
        game.getPlotBar().reset(3600); // 1 heure pour l'acte
        game.getQuestBar().reset(50 + randomService.random(100));
        
        // Encumbrance basée sur la force
        int strength = game.getStat("STR");
        game.getEncumbranceBar().reset(10 + strength, 0);
    }
    
    /**
     * Calcule le temps nécessaire pour le prochain niveau
     */
    private int calculateLevelUpTime(int level) {
        // Formule simple: level * 1000 (peut être ajustée)
        return level * 1000;
    }
    
    /**
     * Démarre le jeu
     */
    public void startGame(Game game) {
        if (gameRunning) {
            logger.warn("Tentative de démarrage d'un jeu déjà en cours");
            return;
        }
        
        this.currentGame = game;
        this.gameRunning = true;
        
        logger.info("Démarrage du jeu pour {}", game.getTrait("Name"));
        
        // Démarrage du timer de jeu - exécution toutes les 100ms
        scheduler.scheduleAtFixedRate(this::gameLoop, 0, 100, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Stoppe le jeu
     */
    public void stopGame() {
        if (!gameRunning) {
            return;
        }
        
        gameRunning = false;
        logger.info("Arrêt du jeu");
        
        // Sauvegarde automatique
        if (currentGame != null) {
            saveGameAsync(currentGame);
        }
    }
    
    /**
     * Boucle principale du jeu (appelée périodiquement)
     */
    private void gameLoop() {
        if (!gameRunning || currentGame == null) {
            return;
        }
        
        try {
            // Progression de la tâche actuelle
            currentGame.getTaskBar().increment(100); // 100ms de progression
            
            // Vérification si la tâche est terminée
            if (currentGame.getTaskBar().isDone()) {
                logger.debug("Barre de tâche terminée. Position: {}/{}", 
                    currentGame.getTaskBar().getPosition(), currentGame.getTaskBar().getMax());
                completeCurrentTask();
            }
            
            // Mise à jour des autres barres de progression
            updateProgressBars();
            
        } catch (Exception e) {
            logger.error("Erreur critique dans la boucle de jeu", e);
        }
    }
    
    /**
     * Termine la tâche actuelle et génère la suivante
     */
    private void completeCurrentTask() {
        String taskType = currentGame.getTaskType();
        
        logger.info("Fin de tâche: '{}' [{}]", currentGame.getCurrentTask(), taskType);
        
        try {
            // Pattern Matching avec instanceof (Java 16+) - ici simulé avec des strings
            switch (taskType) {
                case "kill" -> completeKillTask();
                case "buying" -> completeBuyingTask();
                case "selling" -> completeSellingTask();
                case "market" -> completeMarketTask();
                case "heading" -> completeHeadingTask();
                default -> logger.debug("Pas d'action spécifique pour le type de tâche: {}", taskType);
            }
            
            // Génération de la prochaine tâche
            generateNextTask();
        } catch (Exception e) {
            logger.error("Erreur lors de la transition de tâche (type: {})", taskType, e);
            // Tentative de récupération pour éviter la boucle infinie
            createTask("Recovering from error", 2000);
            currentGame.setTaskType("heading");
        }
    }
    
    /**
     * Termine une tâche de combat
     */
    private void completeKillTask() {
        // Gain d'expérience
        int expGain = currentGame.getTaskBar().getMax() / 1000;
        currentGame.getExpBar().increment(expGain);
        
        // Vérification de montée de niveau
        if (currentGame.getExpBar().isDone()) {
            levelUp();
        }
        
        // Progression des quêtes
        if (currentGame.getCurrentAct() >= 1) {
            currentGame.getQuestBar().increment(expGain);
            if (currentGame.getQuestBar().isDone()) {
                completeQuest();
            }
        }
        
        // Progression de l'intrigue
        currentGame.getPlotBar().increment(expGain);
        if (currentGame.getPlotBar().isDone()) {
            completeAct();
        }
        
        // Chance de trouver un objet
        if (randomService.odds(1, 4)) {
            findRandomItem();
        }
    }
    
    /**
     * Termine une tâche d'achat d'équipement
     */
    private void completeBuyingTask() {
        int equipmentCost = calculateEquipmentPrice();
        currentGame.addToInventory("Gold", -equipmentCost);
        
        // Génération d'un nouvel équipement
        String newEquipment = generateEquipment();
        String slot = randomService.pick(List.of("Weapon", "Shield", "Helm", "Hauberk"));
        currentGame.equipItem(slot, newEquipment);
        
        logger.info("Équipement acheté: {} pour {} gold", newEquipment, equipmentCost);
    }
    
    /**
     * Termine une tâche de vente
     */
    private void completeSellingTask() {
        // Logique de vente d'objets de l'inventaire
        var inventory = currentGame.getInventory();
        if (inventory.size() > 1) { // Garde au moins l'or
            var itemToSell = inventory.get(1); // Premier objet après l'or
            int sellPrice = itemToSell.quantity() * (Integer) currentGame.getTrait("Level");
            
            // Bonus si l'objet a un préfixe spécial
            if (itemToSell.name().contains(" of ")) {
                sellPrice *= (1 + randomService.random(10)) * (1 + randomService.random((Integer) currentGame.getTrait("Level")));
            }
            
            inventory.remove(itemToSell);
            currentGame.addToInventory("Gold", sellPrice);
            
            logger.info("Objet vendu: {} pour {} gold", itemToSell.name(), sellPrice);
        }
    }
    
    /**
     * Termine une visite au marché
     */
    private void completeMarketTask() {
        // Continue à vendre jusqu'à ce que l'inventaire soit vide
        completeSellingTask();
    }
    
    /**
     * Termine un déplacement
     */
    private void completeHeadingTask() {
        currentGame.logEvent("Arrivé sur le terrain de chasse");
    }
    
    /**
     * Génère la prochaine tâche
     */
    private void generateNextTask() {
        logger.debug("Analyse de la prochaine tâche à générer...");

        // Vérification de l'encombrement
        if (currentGame.getEncumbranceBar().isDone()) {
            logger.info("Inventaire plein -> Retour au marché");
            createTask("Heading to market to sell loot", 4000);
            currentGame.setTaskType("market");
            return;
        }
        
        // Vérification de l'argent pour acheter de l'équipement
        int goldAmount = getInventoryItem("Gold")
            .map(Game.InventoryItem::quantity)
            .orElse(0);
            
        int equipPrice = calculateEquipmentPrice();
        if (goldAmount > equipPrice) {
            logger.info("Assez d'or ({}) pour équipement ({}) -> Achat", goldAmount, equipPrice);
            createTask("Negotiating purchase of better equipment", 5000);
            currentGame.setTaskType("buying");
            return;
        }
        
        // Par défaut: aller chasser
        if (!currentGame.getTaskType().equals("heading") && !currentGame.getTaskType().equals("kill")) {
            logger.info("Déplacement vers le terrain de chasse");
            createTask("Heading to the killing fields", 4000);
            currentGame.setTaskType("heading");
            return;
        }
        
        // Génération d'une tâche de combat
        logger.info("Génération d'un combat");
        generateCombatTask();
    }
    
    /**
     * Génère une tâche de combat contre un monstre
     */
    private void generateCombatTask() {
        int playerLevel = (Integer) currentGame.getTrait("Level");
        var monsterTask = generateMonsterTask(playerLevel);
        
        // Calcul sécurisé de la durée (évite la division par zéro et les durées nulles)
        long rawDuration = (2L * 3L * monsterTask.level() * 1000L) / Math.max(1, playerLevel);
        int taskDuration = (int) Math.max(100, rawDuration); // Minimum 100ms
        
        logger.debug("Monstre: {} (Niv {}), Durée calculée: {}ms", 
            monsterTask.description(), monsterTask.level(), taskDuration);

        createTask("Executing " + monsterTask.description(), taskDuration);
        currentGame.setTaskType("kill");
    }
    
    /**
     * Record pour représenter une tâche de monstre
     */
    private record MonsterTask(String description, int level) {}
    
    /**
     * Génère un monstre à combattre
     */
    private MonsterTask generateMonsterTask(int playerLevel) {
        // Variation du niveau du monstre
        int monsterLevel = playerLevel;
        for (int i = playerLevel; i >= 1; i--) {
            if (randomService.odds(2, 5)) {
                monsterLevel += randomService.randomSign();
            }
        }
        monsterLevel = Math.max(1, monsterLevel);
        
        // Sélection d'un monstre approprié
        Constants.Monster monster = selectAppropriateMonster(monsterLevel);
        
        // Modification du nom selon le niveau
        String monsterName = modifyMonsterName(monster, playerLevel);
        
        return new MonsterTask(monsterName, monsterLevel);
    }
    
    /**
     * Sélectionne un monstre approprié au niveau donné
     */
    private Constants.Monster selectAppropriateMonster(int targetLevel) {
        // Pour l'instant, retourne un monstre de base
        // Dans une implémentation complète, cela rechercherait dans Constants.MONSTERS
        return new Constants.Monster("Goblin", targetLevel, "gold");
    }
    
    /**
     * Modifie le nom du monstre selon la différence de niveau
     */
    private String modifyMonsterName(Constants.Monster monster, int playerLevel) {
        int levelDifference = playerLevel - monster.level();
        String baseName = monster.name();

        if (levelDifference <= -10) {
            return "imaginary " + baseName;
        } else if (levelDifference < -5) {
            return "sickly " + baseName;
        } else if (levelDifference < 0 && randomService.odds(1, 2)) {
            return "young " + baseName;
        } else if (levelDifference >= 10) {
            return "messianic " + baseName;
        } else if (levelDifference > 5) {
            return "giant " + baseName;
        } else if (levelDifference > 0 && randomService.odds(1, 2)) {
            return "veteran " + baseName;
        }
        
        // Quantité si approprié
        int quantity = 1;
        if (levelDifference > 10) {
            quantity = Math.max(1, (playerLevel + randomService.random(Math.max(monster.level(), 1))) / Math.max(monster.level(), 1));
        }
        
        return quantity > 1 ? StringUtils.indefinite(baseName, quantity) : StringUtils.indefinite(baseName, 1);
    }
    
    /**
     * Crée une nouvelle tâche
     */
    private void createTask(String description, int durationMs) {
        currentGame.setCurrentTask(description + "...");
        currentGame.getTaskBar().reset(durationMs);
        currentGame.logEvent(description);
        
        logger.debug("Nouvelle tâche: {} ({}ms)", description, durationMs);
    }
    
    /**
     * Gère la montée de niveau
     */
    private void levelUp() {
        int newLevel = (Integer) currentGame.getTrait("Level") + 1;
        currentGame.setTrait("Level", newLevel);
        
        // Augmentation des HP et MP
        int conBonus = currentGame.getStat("CON") / 3;
        int intBonus = currentGame.getStat("INT") / 3;
        
        currentGame.addToStat("HP Max", conBonus + 1 + randomService.random(4));
        currentGame.addToStat("MP Max", intBonus + 1 + randomService.random(4));
        
        // Amélioration de statistiques aléatoires
        improveTwoRandomStats();
        
        // Nouveau sort
        learnRandomSpell();
        
        // Reset de la barre d'expérience
        currentGame.getExpBar().reset(calculateLevelUpTime(newLevel));
        
        // Événement de montée de niveau
        fireGameEvent(new Constants.GameEvent.LevelUp(newLevel));
        
        logger.info("Montée de niveau! Niveau {} atteint", newLevel);
    }
    
    /**
     * Améliore deux statistiques aléatoirement
     */
    private void improveTwoRandomStats() {
        for (int i = 0; i < 2; i++) {
            if (randomService.odds(1, 2)) {
                // Stat aléatoire
                String stat = randomService.pick(Constants.STATS);
                currentGame.addToStat(stat, 1);
            } else {
                // Favorise la meilleure stat pour créer une spécialisation
                String bestStat = findBestPrimaryStat();
                currentGame.addToStat(bestStat, 1);
            }
        }
    }
    
    /**
     * Trouve la meilleure statistique primaire
     */
    private String findBestPrimaryStat() {
        return Constants.PRIME_STATS.stream()
            .max((stat1, stat2) -> Integer.compare(
                currentGame.getStat(stat1), 
                currentGame.getStat(stat2)))
            .orElse("STR");
    }
    
    /**
     * Apprend un sort aléatoire
     */
    private void learnRandomSpell() {
        if (!Constants.SPELLS.isEmpty()) {
            int wisdomLevel = currentGame.getStat("WIS");
            int playerLevel = (Integer) currentGame.getTrait("Level");
            int maxSpellIndex = Math.min(wisdomLevel + playerLevel, Constants.SPELLS.size());
            
            if (maxSpellIndex > 0) {
                String spell = randomService.pickLow(Constants.SPELLS.subList(0, maxSpellIndex));
                currentGame.addSpell(spell, "I"); // Niveau romain I
            }
        }
    }
    
    /**
     * Complete une quête
     */
    private void completeQuest() {
        currentGame.getQuestBar().reset(50 + randomService.random(100));
        
        // Récompenses aléatoires
        int rewardType = randomService.random(4);
        switch (rewardType) {
            case 0 -> learnRandomSpell();
            case 1 -> {
                String equipment = generateEquipment();
                String slot = randomService.pick(List.of("Weapon", "Shield", "Helm"));
                currentGame.equipItem(slot, equipment);
            }
            case 2 -> improveTwoRandomStats();
            case 3 -> findRandomItem();
        }
        
        // Génération d'une nouvelle quête
        String newQuest = generateQuest();
        currentGame.getQuests().add(newQuest);
        currentGame.setBestQuest(newQuest);
        
        fireGameEvent(new Constants.GameEvent.QuestComplete(newQuest));
        
        logger.info("Quête terminée! Nouvelle quête: {}", newQuest);
    }
    
    /**
     * Génère une nouvelle quête
     */
    private String generateQuest() {
        int questType = randomService.random(5);
        return switch (questType) {
            case 0 -> "Exterminate the " + StringUtils.plural(randomService.pick(List.of("Goblin", "Orc", "Troll")));
            case 1 -> "Seek the " + generateMagicalItem();
            case 2 -> "Deliver this " + randomService.pick(Constants.BORING_ITEMS);
            case 3 -> "Fetch me " + StringUtils.indefinite(randomService.pick(Constants.BORING_ITEMS), 1);
            case 4 -> "Placate the " + StringUtils.plural(randomService.pick(List.of("Dragon", "Giant", "Wizard")));
            default -> "Unknown quest";
        };
    }
    
    /**
     * Complete un acte de l'histoire
     */
    private void completeAct() {
        currentGame.setCurrentAct(currentGame.getCurrentAct() + 1);
        int newAct = currentGame.getCurrentAct();
        
        // Reset de la barre d'intrigue
        currentGame.getPlotBar().reset(3600 * (1 + 5 * newAct)); // Plus long à chaque acte
        
        // Ajout de l'acte aux plots
        String actName = "Act " + StringUtils.toRoman(newAct);
        currentGame.getPlots().add(actName);
        currentGame.setBestPlot(actName);
        
        // Récompenses pour les actes
        if (newAct > 1) {
            findRandomItem();
            String equipment = generateEquipment();
            currentGame.equipItem("Weapon", equipment);
        }
        
        logger.info("Acte {} terminé!", newAct);
    }
    
    /**
     * Trouve un objet aléatoire
     */
    private void findRandomItem() {
        String item;
        if (randomService.odds(1, 4)) {
            item = generateMagicalItem();
        } else {
            item = randomService.pick(Constants.BORING_ITEMS);
        }
        
        currentGame.addToInventory(item, 1);
        fireGameEvent(new Constants.GameEvent.ItemFound(item));
    }
    
    /**
     * Génère un objet magique
     */
    private String generateMagicalItem() {
        String adjective = randomService.pick(Constants.ITEM_ATTRIB);
        String base = randomService.pick(Constants.SPECIALS);
        String suffix = randomService.pick(Constants.ITEM_OFS);
        
        return adjective + " " + base + " of " + suffix;
    }
    
    /**
     * Génère un équipement
     */
    private String generateEquipment() {
        int playerLevel = (Integer) currentGame.getTrait("Level");
        
        // Sélection d'un équipement de base
        String baseName = randomService.pick(Constants.WEAPONS); // Simplifié
        String[] parts = baseName.split("\\|");
        String name = parts[0];
        int baseLevel = parts.length > 1 ? Integer.parseInt(parts[1]) : playerLevel;
        
        // Modification selon le niveau
        int levelDiff = playerLevel - baseLevel;
        if (levelDiff != 0) {
            name = (levelDiff > 0 ? "+" : "") + levelDiff + " " + name;
        }
        
        // Ajout possible d'enchantements
        if (randomService.odds(1, 3)) {
            String enchantment = randomService.pick(Constants.OFFENSE_ATTRIB).split("\\|")[0];
            name = enchantment + " " + name;
        }
        
        return name;
    }
    
    /**
     * Met à jour les barres de progression
     */
    private void updateProgressBars() {
        // Mise à jour de l'encombrement basé sur l'inventaire
        int totalWeight = currentGame.getInventory().stream()
            .filter(item -> !item.name().equals("Gold"))
            .mapToInt(Game.InventoryItem::quantity)
            .sum();
        currentGame.getEncumbranceBar().setPosition(totalWeight);
    }
    
    /**
     * Calcule le prix de l'équipement
     */
    private int calculateEquipmentPrice() {
        int level = (Integer) currentGame.getTrait("Level");
        return 5 * level * level + 10 * level + 20;
    }
    
    /**
     * Obtient un objet de l'inventaire par nom
     */
    private Optional<Game.InventoryItem> getInventoryItem(String name) {
        return currentGame.getInventory().stream()
            .filter(item -> item.name().equals(name))
            .findFirst();
    }
    
    /**
     * Lance un événement de jeu
     */
    private void fireGameEvent(Constants.GameEvent event) {
        if (eventListener != null) {
            // CompletableFuture (Java 8+) pour l'exécution asynchrone
            CompletableFuture.runAsync(() -> eventListener.onGameEvent(event));
        }
    }
    
    /**
     * Sauvegarde le jeu de manière asynchrone
     */
    public CompletableFuture<Void> saveGameAsync(Game game) {
        return CompletableFuture.runAsync(() -> {
            try {
                game.saveRandomState(); // Sauvegarde de l'état du générateur aléatoire
                storageService.saveGame(game);
                logger.info("Jeu sauvegardé: {}", game.getTrait("Name"));
            } catch (Exception e) {
                logger.error("Erreur lors de la sauvegarde", e);
            }
        });
    }
    
    /**
     * Charge un jeu de manière asynchrone
     */
    public CompletableFuture<Game> loadGameAsync(String characterName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Game game = storageService.loadGame(characterName);
                logger.info("Jeu chargé: {}", characterName);
                return game;
            } catch (Exception e) {
                logger.error("Erreur lors du chargement", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    // Getters et setters
    public void setEventListener(GameEventListener listener) {
        this.eventListener = listener;
    }
    
    public Game getCurrentGame() {
        return currentGame;
    }
    
    public boolean isGameRunning() {
        return gameRunning;
    }
    
    /**
     * Nettoie les ressources
     */
    public void shutdown() {
        stopGame();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}