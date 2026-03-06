package com.JProgressQuest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.JProgressQuest.service.RandomService;

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
    private Map<String, Object> traits = new HashMap<>();
    
    @JsonProperty("Stats") 
    private Map<String, Integer> stats = new HashMap<>();
    
    @JsonProperty("Equips")
    private Map<String, String> equipment = new HashMap<>();
    
    @JsonProperty("Inventory")
    private List<InventoryItem> inventory = new ArrayList<>();
    
    @JsonProperty("Spells")
    private List<SpellEntry> spells = new ArrayList<>();
    
    @JsonProperty("Quests")
    private List<String> quests = new ArrayList<>();
    
    @JsonProperty("Plots")
    private List<String> plots = new ArrayList<>();
    
    // Barres de progression
    @JsonProperty("ExpBar")
    private ProgressBarState expBar = new ProgressBarState();
    
    @JsonProperty("PlotBar")
    private ProgressBarState plotBar = new ProgressBarState();
    
    @JsonProperty("QuestBar") 
    private ProgressBarState questBar = new ProgressBarState();
    
    @JsonProperty("TaskBar")
    private ProgressBarState taskBar = new ProgressBarState();
    
    @JsonProperty("EncumBar")
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
        private int position = 0;
        
        @JsonProperty("max")
        private int max = 100;
        
        @JsonProperty("percent")
        private int percent = 0;
        
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
            .filter(item -> item.name().equals(itemName))
            .findFirst();
            
        if (existing.isPresent()) {
            InventoryItem item = existing.get();
            inventory.remove(item);
            inventory.add(new InventoryItem(itemName, item.quantity() + quantity));
        } else {
            inventory.add(new InventoryItem(itemName, quantity));
        }
        
        // Log de l'événement
        logEvent(String.format("Ajouté %d %s à l'inventaire", quantity, itemName));
    }
    
    /**
     * Ajoute un sort au livre de sorts
     */
    public void addSpell(String spellName, String level) {
        spells.add(new SpellEntry(spellName, level));
        logEvent("Nouveau sort appris: " + spellName + " " + level);
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
    public String getSpellNameWithMaxLevel(List<SpellEntry> spells) { // TO DO changer le type du level vers int avec transformation Roman à la fin
        return spells.stream()
            .filter(spell -> spell.level() != null && !spell.level().isEmpty())
            .max(Comparator.comparing(spell -> {
                try {
                    return Integer.parseInt(spell.level());
                } catch (NumberFormatException e) {
                    return Integer.MIN_VALUE; // Traiter comme niveau minimum en cas d'erreur
                }
            }))
            .map(SpellEntry::name)
            .orElse("");
    }
     
    public String getBestSpell() {
        bestSpell = getSpellNameWithMaxLevel(spells);
        return bestSpell;
    }

    public String getBestEquipment() {        
        // Convertir en liste et récupérer un élément aléatoire
        List<String> values = new ArrayList<>(equipment.values());
        String randomValue = values.get(new Random().nextInt(values.size()));
        return randomValue; 
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
    public void setInventory(List<InventoryItem> inventory) { this.inventory = inventory; }
    
    public List<SpellEntry> getSpells() { return spells; }
    public void setSpells(List<SpellEntry> spells) { this.spells = spells; }
    
    public List<String> getQuests() { return quests; }
    public void setQuests(List<String> Quests) { this.quests = quests; }
    
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
