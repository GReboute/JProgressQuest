package com.JProgressQuest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.JProgressQuest.service.RandomService;
import com.JProgressQuest.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Modèle principal du jeu Progress Quest.
 * Utilise les annotations Jackson pour la sérialisation JSON et les fonctionnalités Java 17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Game {
    
    // Informations de base du personnage
    @JsonProperty("Traits")
    @JsonAlias("traits")
    private Map<String, Object> traits = new HashMap<>();
    
    @JsonProperty("Stats") 
    @JsonAlias("stats")
    private Map<String, Integer> stats = new HashMap<>();
    
    @JsonProperty("Equips")
    @JsonAlias({"equips", "Equipment"})
    private Map<String, String> equipment = new HashMap<>();
    
    @JsonProperty("Inventory")
    @JsonAlias("inventory")
    private List<InventoryItem> inventory = new ArrayList<>();
    
    @JsonProperty("Spells")
    @JsonAlias("spells")
    private List<SpellEntry> spells = new ArrayList<>();
    
    // La sérialisation est gérée par un getter/setter personnalisé
    private List<String> quests = new ArrayList<>();
    
    @JsonProperty("Plots")
    @JsonAlias("plots")
    private List<String> plots = new ArrayList<>();
    
    // Barres de progression
    @JsonProperty("ExpBar")
    @JsonAlias({"expBar", "expbar"})
    private ProgressBarState expBar = new ProgressBarState();
    
    @JsonProperty("PlotBar")
    @JsonAlias({"plotBar", "plotbar"})
    private ProgressBarState plotBar = new ProgressBarState();
    
    @JsonProperty("QuestBar") 
    @JsonAlias({"questBar", "questbar"})
    private ProgressBarState questBar = new ProgressBarState();
    
    @JsonProperty("TaskBar")
    @JsonAlias({"taskBar", "taskbar"})
    private ProgressBarState taskBar = new ProgressBarState();
    
    @JsonProperty("EncumBar")
    @JsonAlias({"encumBar", "encumbar"})
    private ProgressBarState encumbranceBar = new ProgressBarState();
    
    // État du jeu
    @JsonProperty("act")
    private int currentAct = 0;
    
    @JsonProperty("tasks")
    private int completedTasks = 0;
    
    @JsonProperty("elapsed")
    private long elapsedTime = 0;
    
    @JsonProperty("kill")
    private String currentTask = "";
    
    @JsonProperty("task")
    private String taskType = "";
    
    @JsonProperty("queue")
    private Deque<String> taskQueue = new ArrayDeque<>(); // ArrayDeque (Java 6+) plus efficace que LinkedList
    
    // État du générateur aléatoire
    @JsonProperty("seed")
    private int[] randomSeed;
    
    @JsonProperty("dna")
    private int[] dnaSequence;
    
    // Métadonnées de sauvegarde
    @JsonProperty("date")
    private String saveDate;
    
    @JsonProperty("stamp")
    private long saveTimestamp;
    
    @JsonProperty("saveName")
    private String saveName;
    
    // Informations en ligne (si applicable)
    @JsonProperty("online")
    private OnlineInfo onlineInfo;
    
    @JsonProperty("guild")
    private String guild;
    
    @JsonProperty("motto")
    private String motto;
    
    // Journal des événements - ConcurrentHashMap (Java 5+) pour thread-safety
    @JsonProperty("log")
    private Map<Long, String> eventLog = new ConcurrentHashMap<>();
    
    // Variables de cache pour les "meilleurs" éléments
    @JsonProperty("bestequip")
    private String bestEquipment = "";
    
    @JsonProperty("bestspell")
    private String bestSpell = "";
    
    @JsonProperty("beststat")
    private String bestStat = "";
    
    @JsonProperty("bestplot")
    private String bestPlot = "";
    
    @JsonProperty("bestquest")
    private String bestQuest = "";
    
    // Variables pour les quêtes de monstres
    @JsonProperty("questmonster")
    private String questMonster = "";
    
    @JsonProperty("questmonsterindex")
    private int questMonsterIndex = -1;
    
    // Service non sérialisé pour la génération aléatoire
    @JsonIgnore
    private transient RandomService randomService;
    
    /**
     * Constructeur par défaut pour Jackson
     */
    public Game() {
        initializeDefaults();
    }
    
    /**
     * Constructeur avec nom de personnage
     */
    public Game(String characterName) {
        this();
        traits.put("Name", characterName);
        saveName = characterName;
    }
    
    /**
     * Initialise les valeurs par défaut
     */
    private void initializeDefaults() {
        // Initialisation des traits de base
        traits.put("Level", 1);
        
        // Initialisation des stats de base
        Constants.STATS.forEach(stat -> stats.put(stat, 10));
        
        // Initialisation de l'équipement
        Arrays.asList("Weapon", "Shield", "Helm", "Hauberk", "Brassairts", 
                     "Vambraces", "Gauntlets", "Gambeson", "Cuisses", "Greaves", "Sollerets")
              .forEach(slot -> equipment.put(slot, ""));
        
        // Ajout de l'or initial
        inventory.add(new InventoryItem("Gold", 100));
        
        // Initialisation des barres de progression
        expBar.setMax(100);
        plotBar.setMax(3600); // 1 heure
        questBar.setMax(50);
        taskBar.setMax(2000);
        encumbranceBar.setMax(20);
        
        // Initialisation du service aléatoire
        randomService = new RandomService();
        randomSeed = randomService.getState();
        
        // Métadonnées
        setTime() ;
    }

    /**
     * Recalcule et synchronise l'état des barres après le chargement.
     * Utilise le pourcentage si la position est manquante ou invalide.
     */
    public void reconcileStatus() {
        // Récupération sécurisée du niveau
        Object levelObj = getTrait("Level");
        int level = (levelObj instanceof Integer) ? (Integer) levelObj : 1;
        
        // On s'assure que les max sont cohérents avec les règles du jeu
        // ExpBar: devrait être level * 1000 (selon la formule du GameService)
        if (expBar.getMax() <= 100) {
            expBar.setMax(level * 1000);
        }
        
        // PlotBar: 3600 par défaut (augmente avec l'acte)
        if (plotBar.getMax() <= 100) {
            plotBar.setMax(3600 * (1 + 5 * currentAct));
        }

        // Synchronisation des positions à partir des pourcentages chargés
        syncBar(expBar);
        syncBar(plotBar);
        syncBar(questBar);
        syncBar(taskBar);
        syncBar(encumbranceBar);
    }

    private void syncBar(ProgressBarState bar) {
        if (bar != null && bar.getMax() > 0 && bar.getPercent() > 0) {
            int calculatedPos = (bar.getPercent() * bar.getMax()) / 100;
            // On synchronise si la position actuelle est 0 ou 100 (valeurs par défaut suspectes)
            // ou si l'écart est significatif par rapport au pourcentage (erreur de conversion)
            if ((bar.getPosition() == 0 || bar.getPosition() == 100) && bar.getPosition() != calculatedPos) {
                bar.setPosition(calculatedPos);
            }
        }
    }

    /**
     * Modifie un trait
     */
    public void setTime() {
        Instant now = Instant.now();
        this.saveTimestamp = now.toEpochMilli();
        this.saveDate = LocalDateTime.ofInstant(now, ZoneId.systemDefault()).toString();
    }

    
    /**
     * Record pour représenter un élément d'inventaire (Java 14+)
     */
    public record InventoryItem(
        @JsonProperty("name") String name,
        @JsonProperty("quantity") int quantity
    ) {
        // Validation dans le constructeur compact
        public InventoryItem {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom de l'objet ne peut pas être vide");
            }
            if (quantity < 0) {
                throw new IllegalArgumentException("La quantité ne peut pas être négative");
            }
        }
    }
    
    /**
     * Record pour représenter une entrée de sort
     */
    public record SpellEntry(
        @JsonProperty("name") String name,
        @JsonProperty("level") String level // Roman numeral as string
    ) {}
    
    /**
     * Classe pour représenter l'état d'une barre de progression
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProgressBarState {
        @JsonProperty("position")
        @JsonAlias({"Position", "pos", "Pos"})
        private int position = 0;
        
        @JsonProperty("max")
        @JsonAlias({"Max", "m"})
        private int max = 100;
        
        @JsonProperty("percent")
        private int percent = 0;
        
        public void setPercent(int percent) { this.percent = percent; }

        @JsonProperty("remaining")
        private int remaining = 100;
        
        @JsonProperty("time")
        private String timeRemaining = "";
        
        @JsonProperty("hint")
        private String hint = "";
        
        // Getters et setters avec validation
        public int getPosition() { return position; }
        
        public void setPosition(int position) {
            this.position = Math.max(0, Math.min(position, max));
            updateDerivedValues();
        }
        
        public int getMax() { return max; }
        
        public void setMax(int max) {
            if (max < 1) {
                throw new IllegalArgumentException("Max doit être positif");
            }
            this.max = max;
            updateDerivedValues();
        }
        
        @JsonIgnore
        public boolean isDone() {
            return position >= max;
        }
        
        public void increment(int amount) {
            setPosition(position + amount);
        }
        
        public void reset(int newMax, int newPosition) {
            this.max = newMax;
            setPosition(newPosition);
        }
        
        public void reset(int newMax) {
            reset(newMax, 0);
        }
        
        private void updateDerivedValues() {
            if (max > 0) {
                percent = (position * 100) / max;
                remaining = max - position;
            }
        }
        
        // Getters pour les valeurs calculées
        public int getPercent() { return percent; }
        public int getRemaining() { return remaining; }
        public String getTimeRemaining() { return timeRemaining; }
        public String getHint() { return hint; }
        public void setHint(String hint) { this.hint = hint; }
    }
    
    /**
     * Classe pour les informations de jeu en ligne
     */
    public static class OnlineInfo {
        @JsonProperty("realm")
        private String realm;
        
        @JsonProperty("host")
        private String host;
        
        @JsonProperty("passkey")
        private int passkey;
        
        // Constructeurs, getters et setters
        public OnlineInfo() {}
        
        public OnlineInfo(String realm, String host, int passkey) {
            this.realm = realm;
            this.host = host;
            this.passkey = passkey;
        }
        
        // Getters et setters standards
        public String getRealm() { return realm; }
        public void setRealm(String realm) { this.realm = realm; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPasskey() { return passkey; }
        public void setPasskey(int passkey) { this.passkey = passkey; }
    }
    
    // Méthodes utilitaires pour manipuler les données
    
    /**
     * Ajoute un élément à l'inventaire ou augmente sa quantité
     */
    public void addToInventory(String itemName, int quantity) {
        Optional<InventoryItem> existing = inventory.stream()
            .filter(item -> item.name().equalsIgnoreCase(itemName))
            .findFirst();
            
        if (existing.isPresent()) {
            InventoryItem item = existing.get();
            inventory.remove(item);
            // On utilise le nom existant pour préserver la casse (ex: "Gold")
            inventory.add(new InventoryItem(item.name(), item.quantity() + quantity));
        } else {
            inventory.add(new InventoryItem(itemName, quantity));
        }
        
        // S'assure que l'or est toujours en première position.
        ensureGoldIsFirst();
        
        // Log de l'événement
        logEvent(String.format("Ajouté %d %s à l'inventaire", quantity, itemName));
    }
    
    /**
     * Supprime une pile d'objets de l'inventaire par nom.
     * @param itemName Le nom de l'objet à supprimer.
     * @return true si l'inventaire a été modifié.
     */
    public boolean removeInventoryStack(String itemName) {
        boolean removed = inventory.removeIf(item -> item.name().equalsIgnoreCase(itemName));
        if (removed) {
            logEvent(String.format("Supprimé %s de l'inventaire", itemName));
        }
        return removed;
    }

    /**
     * Méthode privée pour garantir que l'or, s'il existe, est le premier
     * élément de la liste d'inventaire.
     */
    private void ensureGoldIsFirst() {
        Optional<InventoryItem> goldItem = inventory.stream()
            .filter(item -> item.name().equalsIgnoreCase("Gold"))
            .findFirst();

        if (goldItem.isPresent()) {
            InventoryItem gold = goldItem.get();
            // On ne déplace l'élément que s'il n'est pas déjà en première position.
            if (inventory.indexOf(gold) > 0) {
                inventory.remove(gold);
                inventory.add(0, gold);
            }
        }
    }
    
    /**
     * Ajoute un sort au livre de sorts
     * Si le sort est déjà connu, son niveau est augmenté.
     */
    public void addSpell(String spellName, String level) {
        Optional<SpellEntry> existingSpell = spells.stream()
            .filter(spell -> spell.name().equalsIgnoreCase(spellName))
            .findFirst();

        if (existingSpell.isPresent()) {
            // Améliorer le sort existant
            SpellEntry oldSpell = existingSpell.get();
            int currentLevel = StringUtils.fromRoman(oldSpell.level());
            String newLevel = StringUtils.toRoman(currentLevel + 1);
            
            spells.remove(oldSpell);
            spells.add(new SpellEntry(spellName, newLevel));
            logEvent("Sort amélioré: " + spellName + " " + newLevel);
        } else {
            // Apprendre un nouveau sort
            spells.add(new SpellEntry(spellName, level));
            logEvent("Nouveau sort appris: " + spellName + " " + level);
        }
    }
    
    /**
     * Équipe un objet dans un slot spécifique
     */
    public void equipItem(String slot, String itemName) {
        equipment.put(slot, itemName);
        bestEquipment = itemName; // Mise à jour du meilleur équipement
        logEvent("Équipé: " + itemName + " dans " + slot);
    }
    
    /**
     * Purge le journal des événements pour ne garder que les maxEntries derniers.
     * @param maxEntries Le nombre maximum d'entrées à conserver.
     */
    public void pruneLog(int maxEntries) {
        if (eventLog == null || eventLog.size() <= maxEntries) return;

        // Tri des clés (timestamps) pour identifier les plus anciennes
        List<Long> keys = new ArrayList<>(eventLog.keySet());
        Collections.sort(keys);

        int toRemove = keys.size() - maxEntries;
        for (int i = 0; i < toRemove; i++) {
            eventLog.remove(keys.get(i));
        }
    }

    /**
     * Ajoute une quête à la liste.
     */
    public void addQuest(String quest) {
        quests.add(quest);
    }
    
    /**
     * Ajoute une entrée au journal des événements
     */
    public void logEvent(String message) {
        eventLog.put(Instant.now().toEpochMilli(), message);
    }
    
    /**
     * Obtient une statistique par nom
     */
    public int getStat(String statName) {
        return stats.getOrDefault(statName, 0);
    }
    
    /**
     * Modifie une statistique
     */
    public void addToStat(String statName, int value) {
        int currentValue = getStat(statName);
        stats.put(statName, currentValue + value);
        
        if (value != 0) {
            String action = value > 0 ? "Gagné" : "Perdu";
            logEvent(String.format("%s %d %s", action, Math.abs(value), statName));
        }
    }
    
    /**
     * Obtient un trait par nom
     */
    public Object getTrait(String traitName) {
        return traits.get(traitName);
    }
    
    /**
     * Modifie un trait
     */
    public void setTrait(String traitName, Object value) {
        traits.put(traitName, value);
    }
    
    /**
     * Récupère le service de génération aléatoire
     * Le recrée si nécessaire (après désérialisation)
     */
    @JsonIgnore
    public RandomService getRandomService() {
        if (randomService == null) {
            randomService = new RandomService();
            if (randomSeed != null) {
                randomService.setState(randomSeed);
            }
        }
        return randomService;
    }
    
    /**
     * Sauvegarde l'état du générateur aléatoire
     */
    public void saveRandomState() {
        if (randomService != null) {
            randomSeed = randomService.getState();
        }
    }

    /**
     * Récupérer le meilleur sort du livre de sorts
     */
    public String getSpellNameWithMaxLevel(List<SpellEntry> spells) {
        return spells.stream()
            .filter(spell -> spell.level() != null && !spell.level().isEmpty())
            .max(Comparator.comparing(spell -> StringUtils.fromRoman(spell.level())))
            .map(SpellEntry::name)
            .orElse("");
    }
     
    public String getBestSpell() {
        bestSpell = getSpellNameWithMaxLevel(spells);
        return bestSpell;
    }

    /**
     * Récupérer un sort en particulier du livre de sorts
     */
    public int getSpellLevelInList(List<SpellEntry> spells, String spellName) {
        String level =  spells.stream()
            .filter(spell -> spellName.equals( spell.name() ) )
            .map(SpellEntry::level)
            .findFirst()
            .orElse("");
        return StringUtils.fromRoman(level);
    }

    public int getSpellLevel(String spell) {
        return getSpellLevelInList(spells, spell);
    }    

    // Helper to extract bonus from equipment name
    @JsonIgnore
    private int getEquipmentBonus(String itemName) {
        if (itemName == null || itemName.isEmpty()) {
            return Integer.MIN_VALUE;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("^[+-]?\\d+").matcher(itemName);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException e) {
                return 0; // Default for non-numeric start
            }
        }
        return 0; // No bonus found
    }

    public String getBestEquipment() {
        return equipment.values().stream()
                .filter(Objects::nonNull)
                .max(Comparator.comparing(this::getEquipmentBonus))
                .orElse("");
    }

    // Getters et setters pour tous les champs (nécessaires pour Jackson)
    // [Les getters/setters standards sont omis pour la brièveté mais seraient présents dans le code complet]
    
    public Map<String, Object> getTraits() { return traits; }
    public void setTraits(Map<String, Object> traits) { this.traits = traits; }
    
    public Map<String, Integer> getStats() { return stats; }
    public void setStats(Map<String, Integer> stats) { this.stats = stats; }
    
    public Map<String, String> getEquipment() { return equipment; }
    public void setEquipment(Map<String, String> equipment) { this.equipment = equipment; }
    
    public List<InventoryItem> getInventory() { return inventory; }
    public void setInventory(List<InventoryItem> inventory) {
        this.inventory = inventory;
        ensureGoldIsFirst(); // Garantit l'ordre après la désérialisation
    }
    
    public List<SpellEntry> getSpells() { return spells; }
    public void setSpells(List<SpellEntry> spells) { this.spells = spells; }
    
    // Getter pour la logique interne et l'UI, retourne la liste complète
    public List<String> getQuests() { return quests; }
    
    // Setter utilisé par Jackson lors de la désérialisation (chargement)
    @JsonSetter("Quests")
    @JsonAlias("quests")
    public void setQuests(List<String> quests) { this.quests = quests != null ? new ArrayList<>(quests) : new ArrayList<>(); }
    
    // Getter spécial utilisé par Jackson lors de la sérialisation (sauvegarde)
    @JsonGetter("Quests")
    private List<String> getQuestsForSave() {
        /* Au cas ou ou voudrait limiter le nombre de quetes sauvegardées
        mais comme c'est lié à l'acte, il ne faut pas faire ça
        if (quests == null || quests.size() <= 10) {
            return quests;
        }
        return new ArrayList<>(quests.subList(quests.size() - 10, quests.size()));
        */
       return quests;
    }
    
    public List<String> getPlots() { return plots; }
    public void setPlots(List<String> plots) { this.plots = plots; }
    
    public String getCurrentTask() { return currentTask; }
    public void setCurrentTask(String currentTask) { this.currentTask = currentTask; }
    
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public String getBestQuest() { return bestQuest; }
    public void setBestQuest(String bestQuest) { this.bestQuest = bestQuest; }

    public String getBestPlot() { return bestPlot; }
    public void setBestPlot(String bestPlot) { this.bestPlot = bestPlot; }

    public int getCurrentAct() { return currentAct; }
    public void setCurrentAct(int currentAct) { this.currentAct = currentAct; }

    public long getSaveTimestamp() { return saveTimestamp; }
    
        
    public ProgressBarState getExpBar() { return expBar; }
    public ProgressBarState getTaskBar() { return taskBar; }
    public ProgressBarState getPlotBar() { return plotBar; }
    public ProgressBarState getQuestBar() { return questBar; }
    public ProgressBarState getEncumbranceBar() { return encumbranceBar; }

    public Deque<String> getTaskQueue() { return taskQueue; }
    
    // Méthode toString pour le debugging
    @Override
    public String toString() {
        return "Game{" +
               "characterName='" + traits.get("Name") + '\'' +
               ", level=" + traits.get("Level") +
               ", currentAct=" + currentAct +
               '}';
    }
}
