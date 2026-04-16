package com.JProgressQuest.service;

import com.JProgressQuest.model.Constants;
import com.JProgressQuest.model.Game;
import com.JProgressQuest.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
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
    private GameSpeed currentGameSpeed = GameSpeed.NORMAL; // Vitesse par défaut
    
    // Gestionnaire d'événements - Interface fonctionnelle (Java 8+)
    @FunctionalInterface
    public interface GameEventListener {
        void onGameEvent(Constants.GameEvent event);
    }
    
    private GameEventListener eventListener;

    /**
     * Énumération des vitesses de jeu possibles.
     */
    public enum GameSpeed {
        SLOW_2X("2x Lente", 0.5),
        NORMAL("Normale (1x)", 1.0),
        FAST_2X("2x Rapide", 2.0),
        FAST_5X("5x Rapide", 5.0),
        FAST_10X("10x Rapide", 10.0),
        FAST_25X("25x Rapide", 25.0),
        FAST_50X("50x Rapide", 50.0),
        FAST_100X("100x Rapide", 100.0);

        private final String displayName;
        private final double multiplier;

        GameSpeed(String displayName, double multiplier) {
            this.displayName = displayName;
            this.multiplier = multiplier;
        }

        public double getMultiplier() { return multiplier; }
        public String getDisplayName() { return displayName; }

        @Override
        public String toString() {
            return displayName;
        }
    }
    
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
        var queue = game.getTaskQueue();
        queue.add("task|10000|Experiencing an enigmatic and foreboding night vision");
        queue.add("task|6000|Much is revealed about that wise old bastard you'd underestimated");
        queue.add("task|6000|A shocking series of events leaves you alone and bewildered, but resolute");
        queue.add("task|4000|Drawing upon an unrealized reserve of determination, you set out on a long and dangerous journey");
        queue.add("plot|2000|Loading");

        // La barre de tâche est "terminée" pour déclencher la première tâche de la file d'attente.
        game.getTaskBar().reset(0);

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
        
        // Sauvegarde automatique synchrone pour garantir l'écriture avant la fermeture de l'application
        if (currentGame != null) {
            try {
                currentGame.saveRandomState();
                storageService.saveGame(currentGame);
                logger.info("Jeu sauvegardé automatiquement: {}", currentGame.getTrait("Name"));
            } catch (Exception e) {
                logger.error("Erreur lors de la sauvegarde automatique à l'arrêt", e);
            }
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
            // Progression de la tâche actuelle en fonction de la vitesse
            int baseTickInterval = 100; // ms, correspond à la fréquence du scheduler
            int incrementAmount = (int) (baseTickInterval * currentGameSpeed.getMultiplier());
            currentGame.getTaskBar().increment(incrementAmount);
            
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
        
        logger.debug("Fin de tâche: '{}' [{}]", currentGame.getCurrentTask(), taskType);
        
        try {
            boolean wasKillTask = "kill".equals(taskType);
            // Calcul unifié du gain de progression (minimum 1)
            int progressAmount = Math.max(1, currentGame.getTaskBar().getMax() / 1000);

            // Actions spécifiques à la fin de tâche
            switch (taskType) {
                case "kill" -> completeKillTask(progressAmount);
                case "buying" -> completeBuyingTask();
                case "selling", "market" -> completeSellingTask();
                case "heading" -> completeHeadingTask();
                default -> logger.warn("Pas d'action spécifique pour le type de tâche: {}", taskType);
            }

            // Progression de l'intrigue (selon la logique JS: si kill task ou act 0)
            if (wasKillTask || currentGame.getCurrentAct() == 0) {
                currentGame.getPlotBar().increment(progressAmount);

                if (currentGame.getPlotBar().isDone()) {
                    logger.info("Plot bar est pleine. Acte actuel: {}", currentGame.getCurrentAct());
                    // Logique demandée: cinématique pour l'acte 0, complétion directe ensuite.
                    // Note: L'original JS lance une cinématique à chaque acte.
                    if (currentGame.getCurrentAct() == 0) {
                        interplotCinematic();
                    } else {
                        completeAct();
                    }
                }
            }

            // Génération de la prochaine tâche
            dequeueOrGenerateNextTask();
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
    private void completeKillTask(int expGain) {
        // Gain d'expérience
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
        
        // On choisit d'abord le slot, puis on génère l'équipement adapté
        String slot = randomService.pick(Constants.EQUIPS);
        String newEquipment = generateEquipment(slot);
        currentGame.equipItem(slot, newEquipment);
        
        logger.info("Équipement acheté: {} pour {} gold sur {}", newEquipment, equipmentCost, slot);
        fireGameEvent(new Constants.GameEvent.EquipmentBought(newEquipment, equipmentCost, slot));
    }
    
    /**
     * Termine une tâche de vente
     */
    private void completeSellingTask() {
        // Logique de vente du premier objet de l'inventaire qui n'est pas de l'or.
        var inventory = currentGame.getInventory();

        Optional<Game.InventoryItem> itemToSellOpt = inventory.stream()
                .filter(item -> !item.name().equalsIgnoreCase("Gold"))
                .findFirst();

        if (itemToSellOpt.isPresent()) {
            var itemToSell = itemToSellOpt.get();
            // Calcul du prix de vente - initialement très faible
            //int sellPrice  = itemToSell.quantity() * (Integer) currentGame.getTrait("Level");
            int gamelevel = (Integer) currentGame.getTrait("Level");
            int sellPrice  = 0; // pour éviter la double déclaration
            if (gamelevel < 10) {
                sellPrice  += itemToSell.quantity() * gamelevel; // formule par défaut
            } else {
            // tentative d'obtenir un prix de plus en plus élevé - de 125 (lvl=10) à 10000 (lvl=100)
                sellPrice  += itemToSell.quantity() * Math.toIntExact(Math.round(gamelevel*Math.pow(10.0, (1+gamelevel/100.0))));
            }
            

            // Bonus si l'objet a un préfixe spécial
            if (itemToSell.name().contains(" of ")) {
                sellPrice *= (1 + randomService.random(10)) * (1 + randomService.random(gamelevel));
            }

            // Utilisation de la méthode encapsulée pour une suppression fiable
            if (currentGame.removeInventoryStack(itemToSell.name())) {
                currentGame.addToInventory("Gold", sellPrice);
                logger.info("Objet vendu: {} pour {} gold", itemToSell.name(), sellPrice);
            } else {
                logger.error("Échec de la vente, l'objet {} n'a pas pu être retiré de l'inventaire.", itemToSell.name());
            }
        }
    }
    
    /**
     * Termine un déplacement
     */
    private void completeHeadingTask() {
        // Pas d'action spécifique, la tâche suivante sera déterminée par dequeueOrGenerateNextTask
        logger.debug("Arrivé à destination.");
    }
    
    /**
     * Génère la prochaine tâche
     */
    private void dequeueOrGenerateNextTask() {
        var queue = currentGame.getTaskQueue();

        if (!queue.isEmpty()) {
            String taskString = queue.poll(); // poll() retrieves and removes the head
            logger.debug("Tâche récupérée de la file d'attente: {}", taskString);
            String[] parts = taskString.split("\\|", 3);
            if (parts.length < 3) {
                logger.warn("Tâche malformée dans la file d'attente: {}", taskString);
                generateNormalTask(); // Tente de récupérer
                return;
            }
            String type = parts[0];
            int duration = Integer.parseInt(parts[1]);
            String description = parts[2];

            if ("plot".equals(type)) {
                completeAct();
                description = "Loading " + currentGame.getBestPlot();
            }
            
            createTask(description, duration);
            currentGame.setTaskType(type);
            return;
        }

        // Si la file est vide, on génère une tâche normale
        generateNormalTask();
    }

    private void generateNormalTask() {
        logger.debug("Génération d'une tâche normale...");
        String previousTaskType = currentGame.getTaskType();

        // Priorité 1: Vendre le butin si l'inventaire est plein ou si on était déjà en train de vendre.
        boolean needsToSell = currentGame.getEncumbranceBar().isDone() || "selling".equals(previousTaskType);
        if (needsToSell) {
            Optional<Game.InventoryItem> itemToSellOpt = currentGame.getInventory().stream()
                    .filter(item -> !item.name().equalsIgnoreCase("Gold"))
                    .findFirst();

            if (itemToSellOpt.isPresent()) {
                // Si on a trouvé un objet à vendre, on s'assure d'être au marché.
                if (!"selling".equals(previousTaskType) && !"heading".equals(previousTaskType)) {
                    logger.info("Inventaire plein. Direction le marché pour vendre le butin.");
                    createTask("Heading to market to sell loot", 4000);
                    currentGame.setTaskType("heading");
                } else {
                    // On est au marché, on vend l'objet.
                    var itemToSell = itemToSellOpt.get();
                    logger.debug("Au marché, vente de: {}.", itemToSell.name());
                    createTask("Selling " + itemToSell.name(), 1000 + randomService.random(1000));
                    currentGame.setTaskType("selling");
                }
                return; // On a une tâche de vente ou de déplacement, on s'arrête là.
            }
        }

        // Priorité 2: Acheter de l'équipement si on a assez d'or.
        int goldAmount = getInventoryItem("Gold")
                .map(Game.InventoryItem::quantity)
                .orElse(0);
        int equipPrice = calculateEquipmentPrice();
        if (goldAmount > equipPrice) {
            logger.debug("Assez d'or ({}) pour équipement ({}) -> Achat", goldAmount, equipPrice);
            createTask("Negotiating purchase of better equipment", 5000);
            currentGame.setTaskType("buying");
            return;
        }

        // Priorité 3: Si rien d'autre à faire, on part à l'aventure.
        if (!"kill".equals(currentGame.getTaskType()) && !"heading".equals(currentGame.getTaskType())) {
            logger.debug("Déplacement vers le terrain de chasse");
            createTask("Heading to the killing fields", 4000);
            currentGame.setTaskType("heading");
            return;
        }

        // On est sur le terrain de chasse, on génère un combat.
        logger.debug("Génération d'un combat");
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
        logger.debug("HP raised to {}", currentGame.getStat("HP Max"));
        logger.debug("MP raised to {}", currentGame.getStat("MP Max"));
        
        // Amélioration de statistiques aléatoires
        improveTwoRandomStats();
        
        // Nouveau sort
        learnRandomSpell();
        
        // Reset de la barre d'expérience
        currentGame.getExpBar().reset(calculateLevelUpTime(newLevel));
        
        // Événement de montée de niveau
        fireGameEvent(new Constants.GameEvent.LevelUp(newLevel));
        
        logger.warn("Montée de niveau! Niveau {} atteint", newLevel); // pas vraiment un warning, mais information importante 
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
                logger.debug("Stat améliorée {} à {}", stat, currentGame.getStat(stat));
            } else {
                // Favorise la meilleure stat pour créer une spécialisation
                String bestStat = findBestPrimaryStat();
                currentGame.addToStat(bestStat, 1);
                logger.debug("Stat améliorée {} à {}", bestStat, currentGame.getStat(bestStat));
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
                logger.debug("Sort appris/améliorée {} à {}", spell, currentGame.getSpellLevel(spell));
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
                String slot = randomService.pick(List.of("Weapon", "Shield", "Helm"));
                String equipment = generateEquipment(slot);
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
     * Génère une cinématique d'inter-acte en ajoutant des tâches à la file d'attente.
     * Équivalent de la fonction InterplotCinematic en JavaScript.
     */
    private void interplotCinematic() {
        logger.info("Début de la cinématique d'inter-acte.");
        var queue = currentGame.getTaskQueue();
        int scenario = randomService.random(3);

        switch (scenario) {
            case 0 -> {
                queue.add("task|1000|Exhausted, you arrive at a friendly oasis in a hostile land");
                queue.add("task|2000|You greet old friends and meet new allies");
                queue.add("task|2000|You are privy to a council of powerful do-gooders");
                queue.add("task|1000|There is much to be done. You are chosen!");
            }
            case 1 -> {
                String nemesis = nameGenerator.generateNamedMonster((Integer) currentGame.getTrait("Level") + 3);
                queue.add("task|1000|Your quarry is in sight, but a mighty enemy bars your path!");
                queue.add("task|4000|A desperate struggle commences with " + nemesis);
                int s = randomService.random(3);
                for (int i = 0; i < randomService.random(1 + currentGame.getCurrentAct() + 1); i++) {
                    s += 1 + randomService.random(2);
                    switch (s % 3) {
                        case 0 -> queue.add("task|2000|Locked in grim combat with " + nemesis);
                        case 1 -> queue.add("task|2000|" + nemesis + " seems to have the upper hand");
                        case 2 -> queue.add("task|2000|You seem to gain the advantage over " + nemesis);
                    }
                }
                queue.add("task|3000|Victory! " + nemesis + " is slain! Exhausted, you lose consciousness");
                queue.add("task|2000|You awake in a friendly place, but the road awaits");
            }
            case 2 -> {
                String impressiveGuy = nameGenerator.generateImpressiveGuy();
                String boringItem = randomService.pick(Constants.BORING_ITEMS);
                queue.add("task|2000|Oh sweet relief! You've reached the kind protection of " + impressiveGuy);
                queue.add("task|3000|There is rejoicing, and an unnerving encounter with " + impressiveGuy + " in private");
                queue.add("task|2000|You forget your " + boringItem + " and go back to get it");
                queue.add("task|2000|What's this!? You overhear something shocking!");
                queue.add("task|2000|Could " + impressiveGuy + " be a dirty double-dealer?");
                queue.add("task|3000|Who can possibly be trusted with this news!? -- Oh yes, of course");
            }
        }
        // La tâche 'plot' déclenchera la complétion de l'acte via dequeueOrGenerateNextTask
        queue.add("plot|1000|Loading");
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
            String equipment = generateEquipment("Weapon");
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
    private String generateEquipment(String slot) {
        int playerLevel = (Integer) currentGame.getTrait("Level");
        
        // Sélection de la liste appropriée selon le slot
        List<String> sourceList;
        if ("Weapon".equals(slot)) {
            sourceList = Constants.WEAPONS;
        } else if ("Shield".equals(slot)) {
            sourceList = Constants.SHIELDS;
        } else {
            sourceList = Constants.ARMORS;
        }
        
        String baseName = randomService.pick(sourceList);
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
            // Choisir la bonne liste d'attributs (OFFENSE pour les armes, DEFENSE pour le reste).
            List<String> attribList = "Weapon".equals(slot) ? Constants.OFFENSE_ATTRIB : Constants.DEFENSE_ATTRIB;
            String enchantment = randomService.pick(attribList).split("\\|")[0];
            
            // Gestion intelligente des préfixes/suffixes
            // Si l'enchantement commence par "of ", c'est un suffixe (ex: "of hacking")
            if (enchantment.startsWith("of ")) {
                name = name + " " + enchantment;
            } else {
                name = enchantment + " " + name;
            }
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
        // Ancienne version du calcul - n'est pas assez cher à haut niveau
        // return 5 * level * level + 10 * level + 20;
        return 5 * (level * level) + ((int) Math.ceil(Math.pow(1.21, level))) + 20;
        /* avec cette formule, le point de bascule se fait au niveau 30
            moins cher en dessous, plus cher au delà
            là, le vendeur est un vrai voleur pour les hauts niveaux
        */
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
    
    public void setGameSpeed(GameSpeed speed) {
        this.currentGameSpeed = Objects.requireNonNull(speed);
        logger.info("Vitesse du jeu réglée sur: {}", speed.getDisplayName());
    }

    public GameSpeed getGameSpeed() {
        return currentGameSpeed;
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