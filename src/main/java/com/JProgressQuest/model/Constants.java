package com.JProgressQuest.model;

import java.util.List;

/**
 * Constantes du jeu Progress Quest converties depuis le JavaScript original.
 * Utilise les nouvelles fonctionnalités de Java 17 comme les Text Blocks et les records.
 */
public final class Constants {
    
    // Text Blocks (Java 15+) - Plus lisible que les concaténations de chaînes
    public static final String REV_STRING = "&rev=6";
    
    // Traits de base du personnage
    public static final List<String> TRAITS = List.of("Name", "Race", "Class", "Level");
    
    // Statistiques principales - List.of() (Java 9+) pour des listes immuables
    public static final List<String> PRIME_STATS = List.of("STR", "CON", "DEX", "INT", "WIS", "CHA");
    
    // Toutes les statistiques (principales + dérivées)
    public static final List<String> STATS = List.of(
        "STR", "CON", "DEX", "INT", "WIS", "CHA", "HP Max", "MP Max"
    );
    
    // Équipements disponibles
    public static final List<String> EQUIPS = List.of("Sollerets");
    
    // Sorts disponibles
    public static final List<String> SPELLS = List.of("Infinite Confusion");
    
    // Attributs d'attaque
    public static final List<String> OFFENSE_ATTRIB = List.of("Vorpal|+7");
    
    // Attributs de défense
    public static final List<String> DEFENSE_ATTRIB = List.of("Custom|+3");
    
    // Boucliers
    public static final List<String> SHIELDS = List.of("Magnetic Field|18");
    
    // Armures
    public static final List<String> ARMORS = List.of("Plasma|30");
    
    // Armes
    public static final List<String> WEAPONS = List.of("Bandyclef|15");
    
    // Objets spéciaux
    public static final List<String> SPECIALS = List.of("Vulpeculum");
    
    // Attributs d'objets
    public static final List<String> ITEM_ATTRIB = List.of("Puissant");
    
    // Préfixes d'objets
    public static final List<String> ITEM_OFS = List.of("Hydragyrum");
    
    // Objets ennuyeux
    public static final List<String> BORING_ITEMS = List.of("writ");
    
    // Monstres avec leur niveau et leur butin
    public static final List<String> MONSTERS = List.of("Wolog|4|lemma");
    
    // Modificateurs de monstres
    public static final List<String> MON_MODS = List.of("+4 * Rex");
    
    // Malus d'attaque
    public static final List<String> OFFENSE_BAD = List.of("Unbalanced|-2");
    
    // Malus de défense
    public static final List<String> DEFENSE_BAD = List.of("Corroded|-3");
    
    // Races avec leurs bonus de stats
    public static final List<String> RACES = List.of("Land Squid|STR,HP Max");
    
    // Classes (appelées "Klasses" dans le JS pour éviter le conflit avec "class")
    public static final List<String> CLASSES = List.of("Vermineer|INT");
    
    // Titres
    public static final List<String> TITLES = List.of("Saint");
    
    // Titres impressionnants
    public static final List<String> IMPRESSIVE_TITLES = List.of("Inquistor");
    
    // Parties de noms pour la génération - Using Text Blocks pour plus de clarté
    public static final List<List<String>> NAME_PARTS = List.of(
        List.of("br", "cr", "z"),
        List.of("a", "e", "i", "o", "u", "ae", "ie"),
        List.of("b", "ck", "x", "z")
    );
    
    // Record (Java 14+) pour représenter un monstre avec ses propriétés
    public record Monster(String name, int level, String loot) {
        // Les records génèrent automatiquement equals, hashCode, toString
        public static Monster fromString(String monsterString) {
            String[] parts = monsterString.split("\\|");
            return new Monster(
                parts[0],
                Integer.parseInt(parts[1]),
                parts.length > 2 ? parts[2] : ""
            );
        }
    }
    
    // Record pour représenter un objet avec ses attributs
    public record Item(String name, int value, String type) {
        public static Item fromString(String itemString) {
            String[] parts = itemString.split("\\|");
            return new Item(
                parts[0],
                parts.length > 1 ? Integer.parseInt(parts[1]) : 0,
                parts.length > 2 ? parts[2] : ""
            );
        }
    }
    
    // Sealed class (Java 17) pour les différents types d'événements de jeu
    public sealed interface GameEvent 
        permits GameEvent.LevelUp, GameEvent.QuestComplete, GameEvent.ItemFound {
        
        record LevelUp(int newLevel) implements GameEvent {}
        record QuestComplete(String questName) implements GameEvent {}
        record ItemFound(String itemName) implements GameEvent {}
    }
    
    // Enum moderne avec méthodes (amélioré depuis Java 5)
    public enum GameState {
        CREATING_CHARACTER,
        PLAYING,
        PAUSED,
        GAME_OVER;
        
        public boolean isActive() {
            return this == PLAYING;
        }
    }

    // seconds
    // 20 minutes for level 1
    // exponential increase after that
    public static final int getLevelUpTime(int level) {
            return (int) Math.round((20 + Math.pow(1.15, level)) * 60);
    }
     
    // Constantes de temps
    public static final class Time {
        public static final int SECOND = 1000;
        public static final int MINUTE = 60 * SECOND;
        public static final int HOUR = 60 * MINUTE;
        
        private Time() {} // Classe utilitaire - constructeur privé
    }

    
    // Constructeur privé pour empêcher l'instanciation
    private Constants() {
        throw new UnsupportedOperationException("Cette classe ne peut pas être instanciée");
    }
}
