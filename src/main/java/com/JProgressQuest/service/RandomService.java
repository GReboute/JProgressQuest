package com.JProgressQuest.service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom; // Java 7+ - Meilleure performance que Random en multi-threading

/**
 * Service de génération de nombres aléatoires basé sur l'algorithme Alea du JavaScript original.
 * Utilise les nouvelles fonctionnalités de Java 17 pour une meilleure performance et lisibilité.
 */
public class RandomService {
    
    // Variables d'état pour l'algorithme Alea
    private double s0, s1, s2;
    private int c;
    private int[] seedState;
    
    // Constantes pour l'algorithme Mash
    private static final double MASH_CONSTANT = 0.02519603282416938;  // 0.02519603282416938
    private static final double ALEA_CONSTANT = 2.328306e-10;         // 2^-32
    private static final int ALEA_MULTIPLIER = 2091639;
    
    /**
     * Constructeur avec graine basée sur l'heure actuelle.
     * Utilise Instant.now() (Java 8+) au lieu de new Date().getTime()
     */
    public RandomService() {
        this(Instant.now().toEpochMilli());
    }
    
    /**
     * Constructeur avec graine spécifique
     */
    public RandomService(long seed) {
        initializeWithSeed(seed);
    }
    
    /**
     * Initialise le générateur avec une graine spécifique
     */
    private void initializeWithSeed(long seed) {
        var mash = new Mash();
        
        // Initialisation des états comme dans le JS original
        s0 = mash.hash(" ");
        s1 = mash.hash(" ");
        s2 = mash.hash(" ");
        c = 1;
        
        // Application de la graine
        String seedStr = Long.toString(seed);
        s0 -= mash.hash(seedStr);
        s1 -= mash.hash(seedStr);
        s2 -= mash.hash(seedStr);
        
        // Si les valeurs deviennent négatives, on les normalise
        if (s0 < 0) s0 += 1;
        if (s1 < 0) s1 += 1;
        if (s2 < 0) s2 += 1;
        
        // Sauvegarde de l'état pour la sérialisation
        this.seedState = new int[]{(int)(s0 * Integer.MAX_VALUE), 
                                  (int)(s1 * Integer.MAX_VALUE),
                                  (int)(s2 * Integer.MAX_VALUE), c};
    }
    
    /**
     * Génère un nombre aléatoire entre 0.0 et 1.0 (algorithme Alea)
     */
    public double random() {
        double t = ALEA_MULTIPLIER * s0 + c * ALEA_CONSTANT;
        s0 = s1;
        s1 = s2;
        c = (int) t;
        s2 = t - c;
        return s2;
    }
    
    /**
     * Génère un entier aléatoire entre 0 et n-1
     */
    public int random(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n doit être positif"); // Validation des paramètres
        }
        return (int) (random() * n);
    }
    
    /**
     * Génère un entier aléatoire 32 bits
     */
    public int randomInt32() {
        return (int) (random() * 0x100000000L);
    }
    
    /**
     * Génère un double avec 53 bits de précision
     */
    public double randomFract53() {
        return random() + (random() * 0x200000L) * 1.11022302e-16;
    }
    
    /**
     * Choisit un élément aléatoire dans une liste
     * Utilise les génériques (Java 5+) pour la sécurité des types
     */
    public <T> T pick(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("La liste ne peut pas être null ou vide");
        }
        return list.get(random(list.size()));
    }
    
    /**
     * Choisit un élément avec une préférence pour les indices plus bas
     */
    public <T> T pickLow(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("La liste ne peut pas être null ou vide");
        }
        int index = Math.min(random(list.size()), random(list.size()));
        return list.get(index);
    }
    
    /**
     * Génère un signe aléatoire (-1 ou +1)
     */
    public int randomSign() {
        return random(2) * 2 - 1;
    }
    
    /**
     * Test de probabilité - retourne true avec une chance de 'chance' sur 'outOf'
     */
    public boolean odds(int chance, int outOf) {
        return random(outOf) < chance;
    }
    
    /**
     * Retourne l'état actuel du générateur pour la sauvegarde
     */
    public int[] getState() {
        return new int[]{
            (int)(s0 * Integer.MAX_VALUE),
            (int)(s1 * Integer.MAX_VALUE), 
            (int)(s2 * Integer.MAX_VALUE),
            c
        };
    }
    
    /**
     * Restaure l'état du générateur depuis une sauvegarde
     */
    public void setState(int[] state) {
        if (state == null || state.length != 4) {
            throw new IllegalArgumentException("L'état doit être un tableau de 4 entiers");
        }
        s0 = (double) state[0] / Integer.MAX_VALUE;
        s1 = (double) state[1] / Integer.MAX_VALUE;
        s2 = (double) state[2] / Integer.MAX_VALUE;
        c = state[3];
        this.seedState = state.clone(); // clone() pour éviter les modifications externes
    }
    
    /**
     * Classe interne implémentant l'algorithme de hachage Mash
     * Utilise une classe interne statique (Java 1.1+) pour l'encapsulation
     */
    private static class Mash {
        private int n = 0xefc8249d;
        
        public double hash(String data) {
            // Enhanced for loop (Java 5+) plus lisible que la boucle classique
            for (char c : data.toCharArray()) {
                n += c;
                double h = MASH_CONSTANT * n;
                // Récupération uniquement des bits fractionnaires
                n = (int) (h * (1L << 32)) >>> 0;
            }
            // Conversion en nombre entre 0 et 1
            return ((long) n >>> 0) * ALEA_CONSTANT;
        }
    }
    
    // Factory method (Java 8+) pour créer des instances avec des configurations prédéfinies
    public static RandomService createDefault() {
        return new RandomService();
    }
    
    public static RandomService createWithSeed(long seed) {
        return new RandomService(seed);
    }
    
    /**
     * Crée une instance thread-safe pour les applications multi-threadées
     */
    public static RandomService createThreadSafe() {
        return new ThreadSafeRandomService();
    }
    
    /**
     * Version thread-safe du RandomService utilisant ThreadLocal (Java 1.2+)
     */
    private static class ThreadSafeRandomService extends RandomService {
        private final ThreadLocal<RandomService> threadLocalRandom = 
            ThreadLocal.withInitial(() -> new RandomService()); // withInitial (Java 8+)
        
        @Override
        public double random() {
            return threadLocalRandom.get().random();
        }
        
        @Override
        public int random(int n) {
            return threadLocalRandom.get().random(n);
        }
        
        // Override des autres méthodes pour utiliser l'instance thread-local
        @Override
        public <T> T pick(List<T> list) {
            return threadLocalRandom.get().pick(list);
        }
    }
}