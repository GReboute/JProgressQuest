package com.JProgressQuest.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.JProgressQuest.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service de sauvegarde et de chargement des parties.
 * Remplace les mécanismes de stockage JavaScript (localStorage, cookies, WebSQL).
 * Utilise les fonctionnalités modernes de Java 17 et Jackson pour JSON.
 */
public class StorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);
    
    // Répertoire de sauvegarde - Path (Java 7+) au lieu de File
    private final Path saveDirectory;
    private final Path rosterFile;
    
    // Jackson ObjectMapper pour la sérialisation JSON
    private final ObjectMapper objectMapper;
    
    // Cache en mémoire des parties - ConcurrentHashMap pour thread-safety
    private final Map<String, Game> gameCache = new ConcurrentHashMap<>();
    private final Map<String, String> rosterCache = new ConcurrentHashMap<>();
    
    // ReadWriteLock (Java 5+) pour la synchronisation fine
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    
    // Configuration
    private final int maxBackups;
    private final boolean enableCache;
    private final int maxLogEntries;
    
    /**
     * Constructeur avec répertoire de sauvegarde personnalisé
     */
    public StorageService(Path saveDirectory, int maxBackups, boolean enableCache, int maxLogEntries) {
        this.saveDirectory = Objects.requireNonNull(saveDirectory);
        this.rosterFile = saveDirectory.resolve("roster.json");
        this.maxBackups = maxBackups;
        this.enableCache = enableCache;
        this.maxLogEntries = maxLogEntries;
        
        // Configuration de Jackson avec les modules modernes
        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule()) // Support des types Java 8+ Time
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .configure(SerializationFeature.INDENT_OUTPUT, true) // JSON formaté
                .build();
        
        // Création du répertoire si nécessaire
        try {
            Files.createDirectories(saveDirectory);
            logger.info("Répertoire de sauvegarde initialisé: {}", saveDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le répertoire de sauvegarde", e);
        }
        
        // Chargement initial du roster
        loadRosterCache();
        
        // Synchronisation pour récupérer les fichiers manquants du roster
        syncRosterWithFiles();
    }
    
    /**
     * Constructeur par défaut avec répertoire dans le dossier utilisateur
     */
    public StorageService() {
        this(getDefaultSaveDirectory(), 5, true, 10000);
    }
    
    /**
     * Constructeur avec paramètres personnalisés
     */
    public StorageService(int maxBackups, boolean enableCache) {
        this(getDefaultSaveDirectory(), maxBackups, enableCache, 10000);
    }
    
    /**
     * Obtient le répertoire de sauvegarde par défaut
     */
    private static Path getDefaultSaveDirectory() {
        // Utilise les propriétés système Java pour un répertoire cross-platform
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".jprogressquest", "saves");
    }
    
    /**
     * Sauvegarde une partie
     */
    public void saveGame(Game game) throws IOException {
        Objects.requireNonNull(game, "Le jeu ne peut pas être null");
        
        String characterName = (String) game.getTrait("Name");
        if (characterName == null || characterName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du personnage ne peut pas être vide");
        }
        
        // Normalisation du nom pour le système de fichiers
        String safeName = sanitizeFileName(characterName);
        Path gameFile = saveDirectory.resolve(safeName + ".json");
        
        // Création d'une sauvegarde si le fichier existe déjà
        if (Files.exists(gameFile) && maxBackups > 0) {
            createBackup(gameFile, safeName);
        }
        
        // Sérialisation et sauvegarde
        cacheLock.writeLock().lock();
        try {
            // Purge le journal des événements avant la sauvegarde pour limiter la taille du fichier
            game.pruneLog(maxLogEntries);
            
            // Mise à jour des métadonnées
            game.setTime();
            
            // Sauvegarde du fichier - Files.write() (Java 7+) plus moderne que FileWriter
            byte[] jsonData = objectMapper.writeValueAsBytes(game);
            Files.write(gameFile, jsonData, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            
            // Mise à jour du cache
            if (enableCache) {
                gameCache.put(characterName, game);
            }
            
            // Mise à jour du roster
            updateRoster(characterName, createGameSummary(game));
            
            logger.info("Partie sauvegardée: {} ({} bytes) sur {}", characterName, jsonData.length, gameFile.toString());
            
        } finally {
            cacheLock.writeLock().unlock();
        }
    }
    
    /**
     * Charge une partie
     */
    public Game loadGame(String characterName) throws IOException {
        Objects.requireNonNull(characterName, "Le nom du personnage ne peut pas être null");
        
        // Vérification du cache en premier
        cacheLock.readLock().lock();
        try {
            if (enableCache && gameCache.containsKey(characterName)) {
                logger.debug("Partie chargée depuis le cache: {}", characterName);
                return gameCache.get(characterName);
            }
        } finally {
            cacheLock.readLock().unlock();
        }
        
        // Chargement depuis le disque
        String safeName = sanitizeFileName(characterName);
        Path gameFile = saveDirectory.resolve(safeName + ".json");
        
        if (!Files.exists(gameFile)) {
            throw new IOException("Fichier de sauvegarde non trouvé: " + characterName);
        }
        
        cacheLock.writeLock().lock();
        try {
            // Désérialisation - Files.readAllBytes() (Java 7+)
            byte[] jsonData = Files.readAllBytes(gameFile);
            Game game = objectMapper.readValue(jsonData, Game.class);
            
            // Réconciliation de l'état des barres après chargement
            game.reconcileStatus();
            
            // Mise à jour du cache
            if (enableCache) {
                gameCache.put(characterName, game);
            }
            
            logger.info("Partie chargée: {} ({} bytes)", characterName, jsonData.length);
            return game;
            
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * Importe un fichier au format .pqw (Base64 du JSON) et l'intègre au roster.
     */
    public Game importPqwFile(Path pqwPath) throws IOException {
        Objects.requireNonNull(pqwPath, "Le chemin du fichier .pqw ne peut pas être null");
        
        byte[] bytes = Files.readAllBytes(pqwPath);
        String content = new String(bytes, StandardCharsets.UTF_8).trim();
        
        byte[] jsonData;
        try {
            // Tente de décoder le Base64 (format standard des fichiers .pqw)
            jsonData = Base64.getDecoder().decode(content);
        } catch (IllegalArgumentException e) {
            // Si ce n'est pas du base64, on tente de lire comme du JSON brut
            jsonData = bytes;
        }

        Game importedGame = objectMapper.readValue(jsonData, Game.class);
        
        // Initialise les barres de progression et l'état interne
        importedGame.reconcileStatus();
        
        // Sauvegarde immédiate pour intégration automatique au roster et au cache
        saveGame(importedGame);
        
        logger.info("Personnage importé avec succès du format .pqw: {}", importedGame.getTrait("Name"));
        return importedGame;
    }

    /**
     * Obtient la liste de tous les personnages sauvegardés
     */
    public List<GameSummary> listGames() throws IOException {
        cacheLock.readLock().lock();
        try {
            return rosterCache.entrySet().stream()
                .map(entry -> {
                    try {
                        return objectMapper.readValue(entry.getValue(), GameSummary.class);
                    } catch (Exception e) {
                        logger.warn("Erreur lors du parsing du résumé pour {}: {}", entry.getKey(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(GameSummary::lastSaved).reversed()) // Plus récent en premier
                .collect(Collectors.toList());
        } finally {
            cacheLock.readLock().unlock();
        }
    }
    
    /**
     * Supprime une sauvegarde
     */
    public boolean deleteGame(String characterName) throws IOException {
        Objects.requireNonNull(characterName, "Le nom du personnage ne peut pas être null");
        
        String safeName = sanitizeFileName(characterName);
        Path gameFile = saveDirectory.resolve(safeName + ".json");
        
        cacheLock.writeLock().lock();
        try {
            // Suppression du fichier
            boolean deleted = Files.deleteIfExists(gameFile);
            
            if (deleted) {
                // Suppression du cache
                gameCache.remove(characterName);
                
                // Mise à jour du roster
                rosterCache.remove(characterName);
                saveRosterCache();
                
                logger.info("Partie supprimée: {}", characterName);
            }
            
            return deleted;
        } finally {
            cacheLock.writeLock().unlock();
        }
    }
    
    /**
     * Vérifie si une sauvegarde existe
     */
    public boolean gameExists(String characterName) {
        if (characterName == null) return false;
        
        cacheLock.readLock().lock();
        try {
            if (enableCache && gameCache.containsKey(characterName)) {
                return true;
            }
            
            String safeName = sanitizeFileName(characterName);
            Path gameFile = saveDirectory.resolve(safeName + ".json");
            return Files.exists(gameFile);
        } finally {
            cacheLock.readLock().unlock();
        }
    }
    
    /**
     * Nettoie le cache (libère la mémoire)
     */
    public void clearCache() {
        cacheLock.writeLock().lock();
        try {
            gameCache.clear();
            logger.info("Cache vidé");
        } finally {
            cacheLock.writeLock().unlock();
        }
    }
    
    /**
     * Exporte une partie vers un fichier spécifique
     */
    public void exportGame(Game game, Path exportPath) throws IOException {
        Objects.requireNonNull(game, "Le jeu ne peut pas être null");
        Objects.requireNonNull(exportPath, "Le chemin d'export ne peut pas être null");
        
        // Création du répertoire parent si nécessaire
        Path parentDir = exportPath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        
        byte[] jsonData = objectMapper.writeValueAsBytes(game);
        Files.write(exportPath, jsonData, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        
        logger.info("Partie exportée vers: {}", exportPath);
    }
    
    /**
     * Importe une partie depuis un fichier
     */
    public Game importGame(Path importPath) throws IOException {
        Objects.requireNonNull(importPath, "Le chemin d'import ne peut pas être null");
        
        if (!Files.exists(importPath)) {
            throw new IOException("Fichier d'import non trouvé: " + importPath);
        }
        
        byte[] jsonData = Files.readAllBytes(importPath);
        Game game = objectMapper.readValue(jsonData, Game.class);
        
        // Réconciliation après import
        game.reconcileStatus();
        
        logger.info("Partie importée depuis: {}", importPath);
        return game;
    }
    
    /**
     * Crée une sauvegarde de sécurité
     */
    private void createBackup(Path originalFile, String safeName) throws IOException {
        // Suppression des anciennes sauvegardes si on dépasse la limite
        cleanOldBackups(safeName);
        
        // Création du nom de sauvegarde avec timestamp
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String backupName = safeName + "_backup_" + timestamp + ".json";
        Path backupPath = saveDirectory.resolve("backups").resolve(backupName);
        
        // Création du répertoire de sauvegarde
        Files.createDirectories(backupPath.getParent());
        
        // Copie du fichier - Files.copy() (Java 7+)
        Files.copy(originalFile, backupPath, StandardCopyOption.REPLACE_EXISTING);
        
        logger.debug("Sauvegarde créée: {}", backupName);
    }
    
    /**
     * Nettoie les anciennes sauvegardes
     */
    private void cleanOldBackups(String safeName) throws IOException {
        Path backupDir = saveDirectory.resolve("backups");
        if (!Files.exists(backupDir)) return;
        
        String prefix = safeName + "_backup_";
        
        // Try-with-resources (Java 7+) pour la gestion automatique des ressources
        try (Stream<Path> files = Files.list(backupDir)) {
            List<Path> backups = files
                .filter(path -> path.getFileName().toString().startsWith(prefix))
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .collect(Collectors.toList());
            
            // Suppression des sauvegardes en excès
            while (backups.size() >= maxBackups) {
                Path oldestBackup = backups.remove(0);
                Files.deleteIfExists(oldestBackup);
                logger.debug("Ancienne sauvegarde supprimée: {}", oldestBackup.getFileName());
            }
        }
    }
    
    /**
     * Synchronise le roster avec les fichiers présents sur le disque.
     * Utile si le fichier roster.json est corrompu ou incomplet.
     */
    private void syncRosterWithFiles() {
        if (!Files.exists(saveDirectory)) return;

        boolean modified = false;
        try (Stream<Path> files = Files.list(saveDirectory)) {
            List<Path> gameFiles = files
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .filter(path -> !path.getFileName().equals(rosterFile.getFileName()))
                .filter(path -> !path.getFileName().toString().contains("_backup_"))
                .collect(Collectors.toList());

            for (Path path : gameFiles) {
                try {
                    // Lecture pour vérifier si le personnage est dans le roster
                    // On charge le jeu complet pour générer un résumé fiable
                    byte[] jsonData = Files.readAllBytes(path);
                    Game game = objectMapper.readValue(jsonData, Game.class);
                    
                    // Assure la cohérence des données pour le résumé du roster
                    game.reconcileStatus();
                    
                    String name = (String) game.getTrait("Name");

                    if (name != null && !rosterCache.containsKey(name)) {
                        String summaryJson = objectMapper.writeValueAsString(createGameSummary(game));
                        rosterCache.put(name, summaryJson);
                        modified = true;
                        logger.info("Personnage restauré dans le roster: {}", name);
                    }
                } catch (Exception e) {
                    // Fichier ignoré (probablement pas une sauvegarde valide ou un autre type de json)
                    logger.info("Fichier ignoré lors de la synchro roster: {}", path.getFileName());
                }
            }

            if (modified) {
                saveRosterCache();
            }
        } catch (IOException e) {
            logger.error("Erreur lors de la synchronisation du roster", e);
        }
    }

    /**
     * Charge le roster depuis le fichier
     */
    private void loadRosterCache() {
        if (!Files.exists(rosterFile)) {
            logger.debug("Fichier roster non trouvé, création d'un nouveau");
            return;
        }
        
        try {
            byte[] jsonData = Files.readAllBytes(rosterFile);
            
            // Utilisation de TypeReference pour les types génériques complexes
            Map<String, String> roster = objectMapper.readValue(jsonData, 
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
            
            rosterCache.clear();
            rosterCache.putAll(roster);
            
            logger.info("Roster chargé avec {} entrées", roster.size());
            
        } catch (IOException e) {
            logger.error("Erreur lors du chargement du roster", e);
        }
    }
    
    /**
     * Sauvegarde le roster
     */
    private void saveRosterCache() throws IOException {
        byte[] jsonData = objectMapper.writeValueAsBytes(rosterCache);
        Files.write(rosterFile, jsonData, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        
        logger.info("Roster sauvegardé avec {} entrées", rosterCache.size());
    }
    
    /**
     * Met à jour le roster avec les informations d'un personnage
     */
    private void updateRoster(String characterName, GameSummary summary) throws IOException {
        String summaryJson = objectMapper.writeValueAsString(summary);
        rosterCache.put(characterName, summaryJson);
        saveRosterCache();
    }
    
    /**
     * Crée un résumé de partie pour le roster
     */
    private GameSummary createGameSummary(Game game) {
        return new GameSummary(
            (String) game.getTrait("Name"),
            (String) game.getTrait("Race"),
            (String) game.getTrait("Class"),
            (Integer) game.getTrait("Level"),
            game.getCurrentAct(),
            game.getSaveTimestamp(),
            game.getBestEquipment(),
            game.getBestSpell()
        );
    }
    
    /**
     * Normalise un nom de fichier pour éviter les problèmes cross-platform
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "unknown";
        
        // Remplacement des caractères interdits - Pattern plus robuste
        return fileName
            .replaceAll("[\\\\/:*?\"<>|]", "_")  // Caractères interdits Windows
            .replaceAll("\\s+", "_")             // Espaces multiples
            .replaceAll("_{2,}", "_")            // Underscores multiples  
            .replaceAll("^_|_$", "")             // Underscores début/fin
            .toLowerCase();
    }
    
    /**
     * Record (Java 14+) pour représenter un résumé de partie dans le roster
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GameSummary(
        String name,
        String race,
        String characterClass,
        int level,
        int act,
        long lastSaved,
        String bestEquipment,
        String bestSpell
    ) {
        // Méthodes utilitaires dans le record
        public String getFormattedLastSaved() {
            return java.time.Instant.ofEpochMilli(lastSaved)
                .atZone(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofLocalizedDateTime(
                    java.time.format.FormatStyle.MEDIUM));
        }
        
        public String getDisplayName() {
            return String.format("%s (Level %d %s %s)", name, level, race, characterClass);
        }
        
        public boolean isRecentlySaved() {
            return System.currentTimeMillis() - lastSaved < 24 * 60 * 60 * 1000; // 24h
        }
    }
    
    /**
     * Exception personnalisée pour les erreurs de storage
     */
    public static class StorageException extends RuntimeException {
        public StorageException(String message) {
            super(message);
        }
        
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Configuration du service de stockage
     */
    public record StorageConfig(
        Path saveDirectory,
        int maxBackups,
        boolean enableCache,
        boolean enableCompression,
        int cacheMaxSize,
        int maxLogEntries
    ) {
        public static StorageConfig defaultConfig() {
            return new StorageConfig(
                getDefaultSaveDirectory(),
                5,
                true,
                false,
                100,
                10000
            );
        }
        
        public static StorageConfig performanceConfig() {
            return new StorageConfig(
                getDefaultSaveDirectory(),
                10,
                true,
                true,
                500,
                5000
            );
        }
    }
    
    /**
     * Statistiques du service de stockage
     */
    public record StorageStats(
        int totalGames,
        int cachedGames,
        long totalSizeBytes,
        int backupCount,
        long cacheHitRate
    ) {}
    
    /**
     * Obtient les statistiques actuelles
     */
    public StorageStats getStats() {
        cacheLock.readLock().lock();
        try {
            long totalSize = 0;
            int backupCount = 0;
            
            // Calcul de la taille totale et du nombre de backups
            try (Stream<Path> files = Files.walk(saveDirectory)) {
                List<Path> allFiles = files
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
                
                for (Path file : allFiles) {
                    try {
                        totalSize += Files.size(file);
                        if (file.getFileName().toString().contains("_backup_")) {
                            backupCount++;
                        }
                    } catch (IOException e) {
                        logger.warn("Erreur lors du calcul de la taille pour {}", file);
                    }
                }
            } catch (IOException e) {
                logger.warn("Erreur lors du parcours du répertoire de sauvegarde", e);
            }
            
            return new StorageStats(
                rosterCache.size(),
                gameCache.size(),
                totalSize,
                backupCount,
                calculateCacheHitRate()
            );
        } finally {
            cacheLock.readLock().unlock();
        }
    }
    
    // Compteurs pour les statistiques de cache
    private long cacheHits = 0;
    private long cacheRequests = 0;
    
    private long calculateCacheHitRate() {
        return cacheRequests == 0 ? 0 : (cacheHits * 100) / cacheRequests;
    }
    
    /**
     * Optimise le stockage (compression, nettoyage, etc.)
     */
    public void optimize() throws IOException {
        logger.debug("Début de l'optimisation du stockage");
        
        cacheLock.writeLock().lock();
        try {
            // Nettoyage des anciennes sauvegardes
            for (String characterName : rosterCache.keySet()) {
                String safeName = sanitizeFileName(characterName);
                cleanOldBackups(safeName);
            }
            
            // Validation et réparation du roster
            validateAndRepairRoster();
            
            // Compactage du cache si nécessaire
            if (gameCache.size() > 100) { // Limite arbitraire
                gameCache.clear();
                logger.info("Cache vidé pour l'optimisation");
            }
            
        } finally {
            cacheLock.writeLock().unlock();
        }
        
        logger.debug("Optimisation terminée");
    }
    
    /**
     * Valide et répare le roster si nécessaire
     */
    private void validateAndRepairRoster() throws IOException {
        Map<String, String> validEntries = new HashMap<>();
        
        for (Map.Entry<String, String> entry : rosterCache.entrySet()) {
            try {
                // Validation de l'entrée JSON
                objectMapper.readValue(entry.getValue(), GameSummary.class);
                
                // Vérification que le fichier de sauvegarde existe
                String safeName = sanitizeFileName(entry.getKey());
                Path gameFile = saveDirectory.resolve(safeName + ".json");
                
                if (Files.exists(gameFile)) {
                    validEntries.put(entry.getKey(), entry.getValue());
                } else {
                    logger.warn("Fichier de sauvegarde manquant pour {}, suppression du roster", entry.getKey());
                }
                
            } catch (Exception e) {
                logger.warn("Entrée roster corrompue pour {}, suppression", entry.getKey());
            }
        }
        
        if (validEntries.size() != rosterCache.size()) {
            rosterCache.clear();
            rosterCache.putAll(validEntries);
            saveRosterCache();
            logger.info("Roster réparé: {} entrées valides sur {}", 
                       validEntries.size(), rosterCache.size());
        }
    }
    
    /**
     * Sauvegarde de sécurité complète
     */
    public void createFullBackup(Path backupDirectory) throws IOException {
        Objects.requireNonNull(backupDirectory, "Le répertoire de sauvegarde ne peut pas être null");
        
        // Création du répertoire de sauvegarde
        Files.createDirectories(backupDirectory);
        
        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        Path fullBackupDir = backupDirectory.resolve("jprogressquest_backup_" + timestamp);
        
        Files.createDirectories(fullBackupDir);
        
        cacheLock.readLock().lock();
        try {
            // Copie de tous les fichiers de sauvegarde
            try (Stream<Path> files = Files.walk(saveDirectory)) {
                files.filter(Files::isRegularFile)
                     .forEach(file -> {
                         try {
                             Path relativePath = saveDirectory.relativize(file);
                             Path targetPath = fullBackupDir.resolve(relativePath);
                             Files.createDirectories(targetPath.getParent());
                             Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                         } catch (IOException e) {
                             logger.warn("Erreur lors de la copie de {}", file, e);
                         }
                     });
            }
            
            logger.info("Sauvegarde complète créée: {}", fullBackupDir);
            
        } finally {
            cacheLock.readLock().unlock();
        }
    }
    
    /**
     * Restaure depuis une sauvegarde complète
     */
    public void restoreFromBackup(Path backupDirectory) throws IOException {
        Objects.requireNonNull(backupDirectory, "Le répertoire de sauvegarde ne peut pas être null");
        
        if (!Files.exists(backupDirectory) || !Files.isDirectory(backupDirectory)) {
            throw new IOException("Répertoire de sauvegarde invalide: " + backupDirectory);
        }
        
        cacheLock.writeLock().lock();
        try {
            // Nettoyage du répertoire actuel
            if (Files.exists(saveDirectory)) {
                try (Stream<Path> files = Files.walk(saveDirectory)) {
                    files.sorted(Comparator.reverseOrder()) // Dossiers en dernier
                         .forEach(path -> {
                             try {
                                 Files.deleteIfExists(path);
                             } catch (IOException e) {
                                 logger.warn("Impossible de supprimer {}", path);
                             }
                         });
                }
            }
            
            // Copie depuis la sauvegarde
            Files.createDirectories(saveDirectory);
            try (Stream<Path> files = Files.walk(backupDirectory)) {
                files.filter(Files::isRegularFile)
                     .forEach(file -> {
                         try {
                             Path relativePath = backupDirectory.relativize(file);
                             Path targetPath = saveDirectory.resolve(relativePath);
                             Files.createDirectories(targetPath.getParent());
                             Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                         } catch (IOException e) {
                             logger.warn("Erreur lors de la restauration de {}", file, e);
                         }
                     });
            }
            
            // Rechargement du cache
            gameCache.clear();
            loadRosterCache();
            
            logger.info("Restauration complète effectuée depuis: {}", backupDirectory);
            
        } finally {
            cacheLock.writeLock().unlock();
        }
    }
    
    /**
     * Migration des données depuis une ancienne version
     */
    public void migrateFromVersion(String oldVersion, Path oldDataDirectory) throws IOException {
        // À implémenter selon les besoins de migration
        logger.info("Migration depuis la version {} non implémentée", oldVersion);
    }
    
    /**
     * Fermeture propre du service
     */
    public void close() {
        cacheLock.writeLock().lock();
        try {
            // Sauvegarde finale du roster
            if (!rosterCache.isEmpty()) {
                try {
                    saveRosterCache();
                } catch (IOException e) {
                    logger.error("Erreur lors de la sauvegarde finale du roster", e);
                }
            }
            
            // Nettoyage des caches
            gameCache.clear();
            rosterCache.clear();
            
            logger.info("Service de stockage fermé proprement");
            
        } finally {
            cacheLock.writeLock().unlock();
        }
    }
    
    /**
     * Implémentation d'AutoCloseable pour les try-with-resources (Java 7+)
     */
    public void autoClose() {
        close();
    }
}
