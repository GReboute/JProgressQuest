package com.JProgressQuest.service;

import com.JProgressQuest.model.Constants;
import java.util.List;

/**
 * Générateur de noms pour les personnages basé sur l'algorithme du JavaScript original.
 * Utilise les fonctionnalités modernes de Java 17 pour une meilleure lisibilité et performance.
 */
public class NameGenerator {
    
    private final RandomService randomService;
    
    // Parties des noms définies comme constantes de classe (Java 17)
    private static final List<String> CONSONANT_STARTS = List.of("br", "cr", "z");
    private static final List<String> VOWELS = List.of("a", "e", "i", "o", "u", "ae", "ie");  
    private static final List<String> CONSONANT_ENDS = List.of("b", "ck", "x", "z");
    
    /**
     * Constructeur prenant un service de génération aléatoire
     * Injection de dépendance pour faciliter les tests
     */
    public NameGenerator(RandomService randomService) {
        this.randomService = randomService;
    }
    
    /**
     * Constructeur par défaut avec un nouveau RandomService
     */
    public NameGenerator() {
        this(new RandomService());
    }
    
    /**
     * Génère un nom aléatoire selon l'algorithme original.
     * Utilise un switch expression (Java 14+) pour plus de clarté
     */
    public String generateName() {
        var nameBuilder = new StringBuilder(); // var (Java 10+) pour l'inférence de type
        
        // Génération de 6 parties (0 à 5 inclus)
        for (int i = 0; i <= 5; i++) {
            String part = switch (i % 3) {
                case 0 -> randomService.pick(CONSONANT_STARTS);
                case 1 -> randomService.pick(VOWELS);  
                case 2 -> randomService.pick(CONSONANT_ENDS);
                default -> throw new IllegalStateException("Impossible: " + (i % 3)); // Ne devrait jamais arriver
            };
            nameBuilder.append(part);
        }
        
        String name = nameBuilder.toString();
        
        // Capitalisation de la première lettre (méthode utilitaire)
        return capitalizeName(name);
    }
    
    /**
     * Génère un nom de monstre impressionnant
     */
    public String generateMonsterName() {
        return generateName() + " the " + generateTitle();
    }
    
    /**
     * Génère un titre aléatoire
     */
    private String generateTitle() {
        // Pour l'instant, utilise les titres des constantes
        // Pourrait être étendu avec plus de logique de génération
        return randomService.pick(Constants.IMPRESSIVE_TITLES);
    }
    
    /**
     * Génère un nom de guilde ou d'organisation
     */
    public String generateGuildName() {
        String adjective = randomService.pick(List.of(
            "Sacred", "Ancient", "Mystical", "Noble", "Dark", "Golden",
            "Silver", "Eternal", "Hidden", "Lost", "Forgotten"
        ));
        
        String noun = randomService.pick(List.of(
            "Order", "Brotherhood", "Guild", "Circle", "Society", "Covenant",
            "Alliance", "Legion", "Council", "Assembly"
        ));
        
        return adjective + " " + noun;
    }
    
    /**
     * Capitalise la première lettre d'une chaîne
     * Utilise les méthodes modernes de String (Java 11+)
     */
    private String capitalizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        
        // String.charAt(0) et substring() - méthode classique mais efficace
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
    
    /**
     * Génère plusieurs noms et retourne le "meilleur" selon des critères
     * Utilise Stream API (Java 8+) pour une programmation fonctionnelle
     */
    public String generateBestName(int attempts) {
        if (attempts <= 0) {
            throw new IllegalArgumentException("Le nombre de tentatives doit être positif");
        }
        
        return java.util.stream.IntStream.range(0, attempts)
            .mapToObj(i -> generateName())
            .max(this::compareName)
            .orElse(generateName()); // fallback si aucun nom n'est généré
    }
    
    /**
     * Compare deux noms selon des critères de "qualité"
     * Critères: longueur, alternance voyelles/consonnes, etc.
     */
    private int compareName(String name1, String name2) {
        // Score basé sur la longueur (entre 6 et 10 caractères idéal)
        int score1 = calculateNameScore(name1);
        int score2 = calculateNameScore(name2);
        
        return Integer.compare(score1, score2);
    }
    
    /**
     * Calcule un score de qualité pour un nom
     */
    private int calculateNameScore(String name) {
        int score = 0;
        
        // Longueur idéale
        int idealLength = 8;
        int lengthPenalty = Math.abs(name.length() - idealLength);
        score -= lengthPenalty;
        
        // Bonus pour l'alternance voyelles/consonnes
        score += countVowelConsonantAlternations(name) * 2;
        
        // Pénalité pour les répétitions
        score -= countRepeatedCharacters(name);
        
        return score;
    }
    
    /**
     * Compte les alternances voyelles/consonnes dans un nom
     */
    private int countVowelConsonantAlternations(String name) {
        if (name.length() < 2) return 0;
        
        int alternations = 0;
        boolean previousWasVowel = isVowel(name.charAt(0));
        
        for (int i = 1; i < name.length(); i++) {
            boolean currentIsVowel = isVowel(name.charAt(i));
            if (currentIsVowel != previousWasVowel) {
                alternations++;
            }
            previousWasVowel = currentIsVowel;
        }
        
        return alternations;
    }
    
    /**
     * Compte les caractères répétés consécutifs
     */
    private int countRepeatedCharacters(String name) {
        if (name.length() < 2) return 0;
        
        int repeated = 0;
        for (int i = 1; i < name.length(); i++) {
            if (name.charAt(i) == name.charAt(i - 1)) {
                repeated++;
            }
        }
        
        return repeated;
    }
    
    /**
     * Vérifie si un caractère est une voyelle
     */
    private boolean isVowel(char c) {
        return "aeiouAEIOU".indexOf(c) != -1;
    }
    
    /**
     * Factory method pour créer un générateur avec une graine spécifique
     * Utile pour les tests reproductibles
     */
    public static NameGenerator withSeed(long seed) {
        return new NameGenerator(new RandomService(seed));
    }
    
    /**
     * Record (Java 14+) pour représenter les paramètres de génération
     */
    public record NameGenerationConfig(
        int minLength,
        int maxLength,
        boolean allowRepeats,
        List<String> customParts
    ) {
        // Validation dans le constructeur compact (Java 14+)
        public NameGenerationConfig {
            if (minLength < 1 || maxLength < minLength) {
                throw new IllegalArgumentException("Longueurs invalides");
            }
        }
        
        // Factory method pour la configuration par défaut
        public static NameGenerationConfig defaultConfig() {
            return new NameGenerationConfig(4, 12, false, List.of());
        }
    }
    
    /**
     * Génère un nom avec une configuration personnalisée
     */
    public String generateName(NameGenerationConfig config) {
        // Implémentation basée sur la configuration
        String name;
        int attempts = 0;
        final int maxAttempts = 100;
        
        do {
            name = generateName();
            attempts++;
        } while (attempts < maxAttempts && 
                (name.length() < config.minLength() || name.length() > config.maxLength()));
        
        return name;
    }
}
    