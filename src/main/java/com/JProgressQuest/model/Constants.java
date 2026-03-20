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
    public static final List<String> EQUIPS = List.of(
        "Weapon", "Shield", "Helm", "Hauberk", "Brassairts", "Vambraces", 
        "Gauntlets", "Gambeson", "Cuisses", "Greaves", "Sollerets"
    );
    
    // Sorts disponibles
    public static final List<String> SPELLS = List.of(
        "Invisible", "Wizard Lock", "Fireball", "Light", "Strength",
        "Stinking Cloud", "Lightning Bolt", "Invisibility", "Ice Storm", "Animate Dead",
        "Cloudkill", "Disintegrate", "Polymorph", "Teleport", "Wish",
        "Magic Missile", "Detect Magic", "Clairvoyance", "Levitation", "Phantasmal Force",
        "Contact Other Plane", "Feeblemind", "Legend Lore", "Enchant Item", "Power Word, Stun",
        "Web", "Haste", "Continual Light", "Water Breathing", "Remove Curse",
        "Mass Invisibility", "Power Word, Kill", "Prismatic Sphere", "Mind Blank", "Gate",
        "Charm Person", "Detect Invisible", "Hold Person", "Fear", "Confusion",
        "Hold Monster", "Magic Jar", "Quest", "Geas", "Mass Charm",
        "Sleep", "Knock", "Slow", "ESP", "Create Food",
        "Anti-Magic Shell", "Death Spell", "Trap", "Conjure Elemental", "Power Word, Blind",
        "Protection from Evil", "Read Magic", "Dispel Magic", "Protection from Normal Missiles", "Cure Light Wounds",
        "Neutralize Poison", "Create Water", "Cure Serious Wounds", "Cause Disease", "Insect Plague",
        "Cure Critical Wounds", "Resurrection", "Part Water", "Heal", "Earthquake",
        "Detect Evil", "Cure Blindness", "Cause Light Wounds", "Remove Fear", "Purify Food & Drink",
        "Cause Serious Wounds", "Flame Strike", "Raise Dead", "Find the Path", "Word of Recall",
        "Light", "Bless", "Find Traps", "Sticks to Snakes", "Speak with Animals",
        "Cause Critical Wounds", "Dispel Evil", "Control Weather", "Regenerate", "Restoration",
        "Darkness", "Curse", "Continual Darkness", "Animate Dead", "Speak with Dead",
        "Feeblemind", "Harm", "Blade Barrier", "Astral Spell", "Holy Word", "Infinite Confusion"
    );
    
    // Attributs d'attaque
    public static final List<String> OFFENSE_ATTRIB = List.of(
        "Vicious|1", "Savage|2", "Brutal|3", "Merciless|4", "Ferocious|5",
        "Cruel|6", "Vorpal|7", "of hacking|8", "of smiting|9", "of annihilation|10",
        "of death|11", "of doom|12", "of shredding|13", "of pain|14", "of dismemberment|15"
    );
    
    // Attributs de défense
    public static final List<String> DEFENSE_ATTRIB = List.of(
        "of protection|1", "of defense|2", "of warding|3", "of guarding|4", "of the sentinel|5",
        "of impenetrability|6", "of invulnerability|7", "custom|3"
    );
    
    // Boucliers
    public static final List<String> SHIELDS = List.of(
        "Buckler|1", "Kite Shield|3", "Tower Shield|5", "Shield of Reflection|7", "Splynx|9",
        "Shield of the Hand|11", "Aegis|13", "Magnetic Field|18"
    );
    
    // Armures
    public static final List<String> ARMORS = List.of(
        "Leather|2", "Ring|4", "Scale|6", "Chain|8", "Splint|10",
        "Plate|12", "Field Plate|14", "Full Plate|16", "Mithril|20", "Adamantium|25",
        "Plasma|30"
    );
    
    // Armes
    public static final List<String> WEAPONS = List.of(
        "Dagger|1", "Mace|2", "Axe|3", "Shortsword|4", "Broadsword|5",
        "Longsword|6", "Scimitar|7", "Two-Handed Sword|8", "Halberd|9", "Morningstar|10",
        "Flail|11", "Warhammer|12", "Battleaxe|13", "Trident|14", "Bandyclef|15"
    );
    
    // Objets spéciaux
    public static final List<String> SPECIALS = List.of(
        "Amulet", "Talisman", "Ring", "Orb", "Rod",
        "Staff", "Wand", "Figurine", "Gimlet", "Vulpeculum"
    );
    
    // Attributs d'objets
    public static final List<String> ITEM_ATTRIB = List.of(
        "Glimmering", "Glittering", "Sparkling", "Shimmering", "Glowing",
        "Radiant", "Scintillating", "Lustrous", "Lambent", "Incandescent",
        "Twinkling", "Flashing", "Shining", "Blazing", "Brilliant",
        "Resplendent", "Effulgent", "Puissant"
    );
    
    // Préfixes d'objets
    public static final List<String> ITEM_OFS = List.of(
        "the Whale", "the Mammoth", "the Mastodon", "the Behemoth", "the Leviathan",
        "the Colossus", "the Titan", "the Gods", "the Heavens", "the Stars",
        "the Spheres", "the Cosmos", "Eternity", "Infinity", "Hydragyrum"
    );
    
    // Objets ennuyeux
    public static final List<String> BORING_ITEMS = List.of(
        "pebble", "twig", "bottlecap", "button", "lint",
        "writ", "receipt", "handkerchief", "worthless currency", "tin foil",
        "string", "thimble", "cork", "bent nail", "used bandage"
    );
    
    // Monstres avec leur niveau et leur butin
    public static final List<String> MONSTERS = List.of(
        "Anhkheg|6|chitin", "Ant|0|antenna", "Ape|4|ass", "Baluchitherium|14|ear", "Beholder|10|eyestalk",
        "Black Pudding|10|saliva", "Blink Dog|4|eyelid", "Cub Scout|1|neckerchief", "Girl Scout|2|cookie", "Boy Scout|3|merit badge",
        "Eagle Scout|4|merit badge", "Bugbear|3|skin", "Bugboar|3|tusk", "Boogie|3|slime", "Camel|2|hump",
        "Carrion Crawler|3|egg", "Catoblepas|6|neck", "Centaur|4|rib", "Centipede|0|leg", "Cockatrice|5|wattle",
        "Couatl|9|wing", "Crayfish|0|antenna", "Demogorgon|53|tentacle", "Jubilex|17|gel", "Manes|1|tooth",
        "Orcus|27|wand", "Succubus|6|bra", "Vrock|8|neck", "Hezrou|9|leg", "Glabrezu|10|collar",
        "Nalfeshnee|11|tusk", "Marilith|7|arm", "Balor|8|whip", "Yeenoghu|25|flail", "Asmodeus|52|leathers",
        "Baalzebul|43|pants", "Barbed Devil|8|flame", "Bone Devil|9|hook", "Dispater|30|matches", "Erinyes|6|thong",
        "Geryon|30|cornucopia", "Malebranche|5|fork", "Ice Devil|11|snow", "Lemure|3|blob", "Pit Fiend|13|seed",
        "Ankylosaurus|9|tail", "Brontosaurus|30|brain", "Diplodocus|24|fin", "Elasmosaurus|15|neck", "Gorgosaurus|13|arm",
        "Iguanadon|6|thumb", "Megalosaurus|12|jaw", "Monoclonius|8|horn", "Pentasaurus|12|head", "Stegosaurus|18|plate",
        "Triceratops|16|horn", "Tyrannosaurus Rex|18|forearm", "Djinn|7|lamp", "Doppelganger|4|face", "Black Dragon|7|*",
        "Plaid Dragon|7|sporrin", "Blue Dragon|9|*", "Beige Dragon|9|*", "Brass Dragon|7|pole", "Tin Dragon|8|*",
        "Bronze Dragon|9|medal", "Chromatic Dragon|16|scale", "Copper Dragon|8|loafer", "Gold Dragon|8|filling", "Green Dragon|8|*",
        "Platinum Dragon|21|*", "Red Dragon|10|cocktail", "Silver Dragon|10|*", "White Dragon|6|tooth", "Dragon Turtle|13|shell",
        "Dryad|2|acorn", "Dwarf|1|drawers", "Eel|2|sashimi", "Efreet|10|cinder", "Sand Elemental|8|glass",
        "Bacon Elemental|10|bit", "Porn Elemental|12|lube", "Cheese Elemental|14|curd", "Hair Elemental|16|follicle", "Swamp Elf|1|lilypad",
        "Brown Elf|1|tusk", "Sea Elf|1|jerkin", "Ettin|10|fur", "Frog|0|leg", "Violet Fungi|3|spore",
        "Gargoyle|4|gravel", "Gelatinous Cube|4|jam", "Ghast|4|vomit", "Ghost|10|*", "Ghoul|2|muscle",
        "Humidity Giant|12|drops", "Beef Giant|11|steak", "Quartz Giant|10|crystal", "Porcelain Giant|9|fixture", "Rice Giant|8|grain",
        "Cloud Giant|12|condensation", "Fire Giant|11|cigarettes", "Frost Giant|10|snowman", "Hill Giant|8|corpse", "Stone Giant|9|hatchling",
        "Storm Giant|15|barometer", "Mini Giant|4|pompadour", "Gnoll|2|collar", "Gnome|1|hat", "Goblin|1|ear",
        "Grid Bug|1|carapace", "Jellyrock|9|seedling", "Beer Golem|15|foam", "Oxygen Golem|17|platelet", "Cardboard Golem|14|recycling",
        "Rubber Golem|16|ball", "Leather Golem|15|fob", "Gorgon|8|testicle", "Gray Ooze|3|gravy", "Green Slime|2|sample",
        "Griffon|7|nest", "Banshee|7|larynx", "Harpy|3|mascara", "Hell Hound|5|tongue", "Hippocampus|4|mane",
        "Hippogriff|3|egg", "Hobgoblin|1|patella", "Homunculus|2|fluid", "Hydra|8|gyrum", "Imp|2|tail",
        "Invisible Stalker|8|*", "Iron Peasant|3|chaff", "Jumpskin|3|shin", "Kobold|1|penis", "Leprechaun|1|wallet",
        "Leucrotta|6|hoof", "Lich|11|crown", "Lizard Man|2|tail", "Lurker|10|sac", "Manticore|6|spike",
        "Mastodon|12|tusk", "Medusa|6|eye", "Multicell|2|dendrite", "Pirate|1|booty", "Berserker|1|shirt",
        "Caveman|2|club", "Dervish|1|robe", "Merman|1|trident", "Mermaid|1|gills", "Mimic|9|hinge",
        "Mind Flayer|8|tentacle", "Minotaur|6|map", "Yellow Mold|1|spore", "Morkoth|7|teeth", "Mummy|6|gauze",
        "Naga|9|rattle", "Nebbish|1|belly", "Neo-Otyugh|11|organ ", "Nixie|1|webbing", "Nymph|3|hanky",
        "Ochre Jelly|6|nucleus", "Octopus|2|beak", "Ogre|4|talon", "Ogre Mage|5|apparel", "Orc|1|snout",
        "Otyugh|7|organ", "Owlbear|5|feather", "Pegasus|4|aileron", "Peryton|4|antler", "Piercer|3|tip",
        "Pixie|1|dust", "Man-o-war|3|tentacle", "Purple Worm|15|dung", "Quasit|3|tail", "Rakshasa|7|pajamas",
        "Rat|0|tail", "Remorhaz|11|protrusion", "Roc|18|wing", "Roper|11|twine", "Rot Grub|1|eggsac",
        "Rust Monster|5|shavings", "Satyr|5|hoof", "Sea Hag|3|wart", "Silkie|3|fur", "Shadow|3|silhouette",
        "Shambling Mound|10|mulch", "Shedu|9|hoof", "Shrieker|3|stalk", "Skeleton|1|clavicle", "Spectre|7|vestige",
        "Sphinx|10|paw", "Spider|0|web", "Sprite|1|can", "Stirge|1|proboscis", "Stun Bear|5|tooth",
        "Stun Worm|2|trode", "Su-monster|5|tail", "Sylph|3|thigh", "Titan|20|sandal", "Trapper|12|shag",
        "Treant|10|acorn", "Triton|3|scale", "Troglodyte|2|tail", "Troll|6|hide", "Umber Hulk|8|claw",
        "Unicorn|4|blood", "Vampire|8|pancreas", "Wight|4|lung", "Will-o'-the-Wisp|9|wisp", "Wraith|5|finger",
        "Wyvern|7|wing", "Xorn|7|jaw", "Yeti|4|fur", "Zombie|2|forehead", "Wasp|0|stinger",
        "Rat|1|tail", "Bunny|0|ear", "Moth|0|dust", "Beagle|0|collar", "Midge|0|corpse",
        "Ostrich|1|beak", "Billy Goat|1|beard", "Bat|1|wing", "Koala|2|heart", "Wolf|2|paw",
        "Whippet|2|collar", "Uruk|2|boot", "Poroid|4|node", "Moakum|8|frenum", "Fly|0|*",
        "Hogbird|3|curl", "Wolog|4|lemma"
    );
    
    // Modificateurs de monstres
    public static final List<String> MON_MODS = List.of(
        "-4 fœtal *",  "-4 dying *",  "-3 crippled *",  "-3 baby *",  "-2 adolescent *",  "-2 very sick *",  "-1 lesser *",
        "-1 undernourished *",  "+1 greater *",  "+1 * Elder",  "+2 war *",  "+2 Battle-*",  "+3 Were-*",  "+3 undead *",  "+4 giant *",
        "+4 * Rex"
    );
    
    // Malus d'attaque
    public static final List<String> OFFENSE_BAD = List.of("Dull|-1", "Unbalanced|-2", "Heavy|-3", "Awkward|-4", "Cursed|-5");
    
    // Malus de défense
    public static final List<String> DEFENSE_BAD = List.of("Torn|-1", "Rusty|-2", "Corroded|-3", "Holey|-4", "Cursed|-5");
    
    // Races avec leurs bonus de stats
    public static final List<String> RACES = List.of("Human|", "Elf|DEX,INT", "Dwarf|CON,STR", "Halfling|DEX,STR", "Gnome|DEX,INT", "Half-Elf|DEX,INT", "Half-Orc|STR,CON", "Barbarian|STR,CON", "Land Squid|STR,HP Max");
    
    // Classes (appelées "Klasses" dans le JS pour éviter le conflit avec "class")
    public static final List<String> CLASSES = List.of("Fighter|STR", "Ranger|DEX", "Paladin|STR,CHA", "Mage|INT", "Cleric|WIS", "Thief|DEX", "Bard|CHA,DEX", "Druid|WIS,CHA", "Enchanter|INT,CHA", "Illusionist|INT,DEX", "Necromancer|INT,CON", "Monk|STR,DEX,CON", "Assassin|DEX,INT", "Vermineer|INT");
    
    // Titres
    public static final List<String> TITLES = List.of("Mr.", "Mrs.", "Sir", "Dame", "Lord", "Lady", "Saint", "King", "Queen", "Emperor", "Empress");
    
    // Titres impressionnants
    public static final List<String> IMPRESSIVE_TITLES = List.of("Grand", "Exalted", "Honorable", "High", "Wise", "Revered", "Venerable", "Pious", "Magnificent", "Omnipotent", "Omniscient", "Infallible", "Unstoppable", "Indomitable", "Eternal", "Inquisitor");
    
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
        permits GameEvent.LevelUp, GameEvent.QuestComplete, GameEvent.ItemFound, GameEvent.EquipmentBought {
        
        record LevelUp(int newLevel) implements GameEvent {}
        record QuestComplete(String questName) implements GameEvent {}
        record ItemFound(String itemName) implements GameEvent {}
        record EquipmentBought(String itemName, int cost, String slot) implements GameEvent {}
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
