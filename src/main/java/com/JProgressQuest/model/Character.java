package com.JProgressQuest.model;

import com.JProgressQuest.model.Constants;
import com.JProgressQuest.util.RandomGenerator;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Represents a character in Progress Quest.
 * This is a Java conversion of the "newguy" object from the original JavaScript code.
 */
public class Character {
    // Character traits
    private Map<String, String> traits;
    
    // Character stats
    private Map<String, Integer> stats;
    
    // Equipment
    private Map<String, String> equips;
    
    // Inventory items [name, quantity]
    private List<Object[]> inventory;
    
    // Spells known
    private List<String> spells;
    
    // Active quests
    private List<String> quests;
    
    // Character metadata
    private double[] seed;
    private String birthday;
    private long birthstamp;
    private String date;
    private long stamp;
    private String beststat;
    private String bestequip;
    
    // Game progress
    private String task;
    private int tasks;
    private long elapsed;
    private int act;
    private String bestplot;
    private String questmonster;
    private String kill;
    
    // Progress bars
    private ProgressBar expBar;
    private ProgressBar encumBar;
    private ProgressBar plotBar;
    private ProgressBar questBar;
    private ProgressBar taskBar;
    
    // Task queue
    private List<String> queue;
    
    // Online mode
    private Map<String, Object> online;
    
    /**
     * Create a new character with random stats
     */
    public Character() {
        RandomGenerator random = new RandomGenerator();
        
        // Initialize collections
        this.traits = new HashMap<>();
        this.stats = new HashMap<>();
        this.equips = new HashMap<>();
        this.inventory = new ArrayList<>();
        this.spells = new ArrayList<>();
        this.quests = new ArrayList<>();
        this.queue = new ArrayList<>();
        
        // Roll stats
        int total = 0;
        int best = -1;
        String bestStatName = "";
        
        for (String stat : Constants.PRIME_STATS) {
            int roll = 3 + random.nextInt(6) + random.nextInt(6) + random.nextInt(6);
            stats.put(stat, roll);
            total += roll;
            
            if (roll > best) {
                best = roll;
                bestStatName = stat;
            }
        }
        
        // Derived stats
        stats.put("HP Max", random.nextInt(8) + Math.floorDiv(stats.get("CON"), 6));
        stats.put("MP Max", random.nextInt(8) + Math.floorDiv(stats.get("INT"), 6));
        
        // Save the random seed
        this.seed = random.getState();
        
        // Generate a name
        traits.put("Name", random.generateName());
        
        // Default race and class (to be selected by user)
        traits.put("Race", "");
        traits.put("Class", "");
        traits.put("Level", "1");
        
        // Set up timestamp
        LocalDateTime now = LocalDateTime.now();
        this.birthday = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.birthstamp = Instant.now().toEpochMilli(); 
        this.date = this.birthday;
        this.stamp = this.birthstamp;
        
        // Best stat
        this.beststat = bestStatName + " " + best;
        
        // Starting equipment
        this.bestequip = "Sharp Rock";
        for (String equip : Constants.EQUIPS) {
            this.equips.put(equip, "");
        }
        this.equips.put("Weapon", this.bestequip);
        this.equips.put("Hauberk", "-3 Burlap");
        
        // Starting inventory
        this.inventory.add(new Object[]{"Gold", 0});
        
        // Game progress
        this.task = "";
        this.tasks = 0;
        this.elapsed = 0;
        this.act = 0;
        this.bestplot = "Prologue";
        this.questmonster = "";
        this.kill = "Loading....";
        
        // Progress bars
        this.expBar = new ProgressBar(0, Constants.getLevelUpTime(1));
        this.encumBar = new ProgressBar(0, stats.get("STR") + 10);
        this.plotBar = new ProgressBar(0, 26);
        this.questBar = new ProgressBar(0, 1);
        this.taskBar = new ProgressBar(0, 2000);
        
        // Starting quest queue
        this.queue.add("task|10|Experiencing an enigmatic and foreboding night vision");
        this.queue.add("task|6|Much is revealed about that wise old bastard you'd underestimated");
        this.queue.add("task|6|A shocking series of events leaves you alone and bewildered, but resolute");
        this.queue.add("task|4|Drawing upon an unrealized reserve of determination, you set out on a long and dangerous journey");
        this.queue.add("plot|2|Loading");
    }
    
    // Getters and setters
    
    public Map<String, String> getTraits() {
        return traits;
    }
    
    public Map<String, Integer> getStats() {
        return stats;
    }
    
    public Map<String, String> getEquips() {
        return equips;
    }
    
    public List<Object[]> getInventory() {
        return inventory;
    }
    
    public List<String> getSpells() {
        return spells;
    }
    
    public List<String> getQuests() {
        return quests;
    }
    
    public double[] getSeed() {
        return seed;
    }
    
    public void setSeed(double[] seed) {
        this.seed = seed;
    }
    
    public String getBirthday() {
        return birthday;
    }
    
    public long getBirthstamp() {
        return birthstamp;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public long getStamp() {
        return stamp;
    }
    
    public void setStamp(long stamp) {
        this.stamp = stamp;
    }
    
    public String getBeststat() {
        return beststat;
    }
    
    public String getBestequip() {
        return bestequip;
    }
    
    public void setBestequip(String bestequip) {
        this.bestequip = bestequip;
    }
    
    public String getTask() {
        return task;
    }
    
    public void setTask(String task) {
        this.task = task;
    }
    
    public int getTasks() {
        return tasks;
    }
    
    public void setTasks(int tasks) {
        this.tasks = tasks;
    }
    
    public long getElapsed() {
        return elapsed;
    }
    
    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }
    
    public int getAct() {
        return act;
    }
    
    public void setAct(int act) {
        this.act = act;
    }
    
    public String getBestplot() {
        return bestplot;
    }
    
    public void setBestplot(String bestplot) {
        this.bestplot = bestplot;
    }
    
    public String getQuestmonster() {
        return questmonster;
    }
    
    public void setQuestmonster(String questmonster) {
        this.questmonster = questmonster;
    }
    
    public String getKill() {
        return kill;
    }
    
    public void setKill(String kill) {
        this.kill = kill;
    }
    
    public ProgressBar getExpBar() {
        return expBar;
    }
    
    public ProgressBar getEncumBar() {
        return encumBar;
    }
    
    public ProgressBar getPlotBar() {
        return plotBar;
    }
    
    public ProgressBar getQuestBar() {
        return questBar;
    }
    
    public ProgressBar getTaskBar() {
        return taskBar;
    }
    
    public List<String> getQueue() {
        return queue;
    }
    
    public Map<String, Object> getOnline() {
        return online;
    }
    
    public void setOnline(Map<String, Object> online) {
        this.online = online;
    }
    
    /**
     * Set character's name
     * @param name New name
     */
    public void setName(String name) {
        traits.put("Name", name);
    }
    
    /**
     * Set character's race
     * @param race New race
     */
    public void setRace(String race) {
        traits.put("Race", race);
    }
    
    /**
     * Set character's class
     * @param characterClass New class
     */
    public void setCharacterClass(String characterClass) {
        traits.put("Class", characterClass);
    }
    
    /**
     * Inner class to track progress bars
     */
    public static class ProgressBar {
        private int position;
        private int max;
        
        public ProgressBar(int position, int max) {
            this.position = position;
            this.max = max;
        }
        
        public int getPosition() {
            return position;
        }
        
        public void setPosition(int position) {
            this.position = position;
        }
        
        public int getMax() {
            return max;
        }
        
        public void setMax(int max) {
            this.max = max;
        }
        
        /**
         * Get progress percentage
         * @return Progress as a value between 0 and 1
         */
        public double getProgress() {
            return (double) position / max;
        }
    }
}
