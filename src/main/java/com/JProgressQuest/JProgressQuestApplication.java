package com.JProgressQuest;

import com.JProgressQuest.controller.MainController;
import com.JProgressQuest.controller.RosterController;
import com.JProgressQuest.service.GameService;
import com.JProgressQuest.service.StorageService;
import com.JProgressQuest.service.NameGenerator;
import com.JProgressQuest.service.RandomService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Application principale JProgressQuest.
 * Point d'entrée de l'application JavaFX avec initialisation des services.
 * Utilise les fonctionnalités modernes de JavaFX 17 et Java 17.
 */
public class JProgressQuestApplication extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(JProgressQuestApplication.class);
    
    // Constantes de l'application
    private static final String APP_TITLE = "JProgressQuest";
    private static final String APP_VERSION = "1.0.1";
    private static final double MIN_WIDTH = 900;
    private static final double MIN_HEIGHT = 700;
    
    // Services de l'application
    private StorageService storageService;
    private GameService gameService;
    
    // Stage principal
    private Stage primaryStage;
    
    /**
     * Point d'entrée principal de l'application
     */
    public static void main(String[] args) {
        // Configuration du système de logging
        System.setProperty("logback.configurationFile", "logback.xml");
        
        // Propriétés JavaFX pour de meilleures performances
        System.setProperty("javafx.animation.fullspeed", "true");
        System.setProperty("prism.lcdtext", "false"); // Améliore le rendu sur certains systèmes
        
        logger.info("Démarrage de {} v{}", APP_TITLE, APP_VERSION);
        logger.info("Java version: {}", System.getProperty("java.version"));
        logger.info("JavaFX version: {}", System.getProperty("javafx.version"));
        
        // Lancement de l'application JavaFX
        launch(args);
    }
    
    /**
     * Initialisation de l'application (appelée avant start())
     */
    @Override
    public void init() throws Exception {
        super.init();
        
        logger.debug("Initialisation des services...");
        
        try {
            // Initialisation des services
            storageService = new StorageService();
            // Injection de dépendances pour s'assurer que tous les services partagent les mêmes instances
            RandomService randomService = new RandomService();
            NameGenerator nameGenerator = new NameGenerator(randomService);
            gameService = new GameService(randomService, nameGenerator, storageService);
            
            logger.debug("Services initialisés avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation des services", e);
            throw e;
        }
    }
    
    /**
     * Démarrage de l'interface utilisateur
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try {
            // Configuration de la fenêtre principale
            setupPrimaryStage(primaryStage);
            
            // Chargement du Roster (Menu Principal) au démarrage
            loadRosterInterface(primaryStage);
            
            // Gestion de la fermeture de l'application
            setupShutdownHandler(primaryStage);
            
            // Affichage de la fenêtre
            primaryStage.show();
            
            logger.debug("Interface utilisateur démarrée");
            
        } catch (Exception e) {
            logger.error("Erreur lors du démarrage de l'interface", e);
            showErrorDialog("Erreur de démarrage", 
                           "Impossible de démarrer l'application", e);
            Platform.exit();
        }
    }
    
    /**
     * Configuration de la fenêtre principale
     */
    private void setupPrimaryStage(Stage stage) {
        stage.setTitle(APP_TITLE + " v" + APP_VERSION);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        
        // Icône de l'application
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            logger.warn("Impossible de charger l'icône de l'application: {}", e.getMessage());
        }
        
        // Position initiale au centre de l'écran
        stage.centerOnScreen();
        
        // Configuration pour une meilleure apparence sur les écrans haute résolution
        stage.getScene(); // Peut être null à ce stade
    }
    
    /**
     * Chargement de l'interface du Roster
     */
    private void loadRosterInterface(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/roster-view.fxml"));
        
        fxmlLoader.setControllerFactory(controllerClass -> {
            if (controllerClass == RosterController.class) {
                return new RosterController(gameService, storageService);
            }
            return null;
        });
        
        javafx.scene.Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, MIN_WIDTH, MIN_HEIGHT);
        
        // Chargement du CSS
        loadStylesheets(scene);
        
        stage.setScene(scene);
        
        RosterController controller = fxmlLoader.getController();
        controller.setStage(stage);
    }
    
    /**
     * Chargement de l'interface principale
     */
    private void loadMainInterface(Stage stage) throws IOException {
        // Chargement du fichier FXML
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/main-view.fxml"));
        
        // Configuration du contrôleur avec injection de dépendances
        fxmlLoader.setControllerFactory(controllerClass -> {
            if (controllerClass == MainController.class) {
                return new MainController(gameService, storageService);
            }
            
            try {
                return controllerClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                logger.error("Impossible de créer le contrôleur: {}", controllerClass, e);
                throw new RuntimeException(e);
            }
        });
        
        // Chargement de la vue
        var root = fxmlLoader.<javafx.scene.layout.VBox>load();
        
        // Création de la scène
        Scene scene = new Scene(root, MIN_WIDTH, MIN_HEIGHT);
        
        // Chargement du CSS
        loadStylesheets(scene);
        
        // Configuration de la scène
        stage.setScene(scene);
        
        // Récupération du contrôleur pour l'initialisation
        MainController controller = fxmlLoader.getController();
        controller.setStage(stage);
    }
    
    /**
     * Chargement des feuilles de style CSS
     */
    private void loadStylesheets(Scene scene) {
        try {
            // Feuille de style principale
            var mainCss = getClass().getResource("/css/application.css");
            if (mainCss != null) {
                scene.getStylesheets().add(mainCss.toExternalForm());
            }
            
            // Feuille de style pour le thème sombre (optionnel)
            var darkThemeCss = getClass().getResource("/css/dark-theme.css");
            if (darkThemeCss != null && isDarkThemePreferred()) {
                scene.getStylesheets().add(darkThemeCss.toExternalForm());
            }
            
            logger.debug("Feuilles de style chargées");
            
        } catch (Exception e) {
            logger.warn("Erreur lors du chargement des feuilles de style: {}", e.getMessage());
        }
    }
    
    /**
     * Vérifie si le thème sombre est préféré
     */
    private boolean isDarkThemePreferred() {
        // Détection du thème système (Java 17+ pourrait utiliser des APIs plus avancées)
        String osName = System.getProperty("os.name").toLowerCase();
        
        // Pour l'instant, retourne false par défaut
        // Dans une version plus avancée, on pourrait détecter le thème du système
        return false;
    }
    
    /**
     * Configuration du gestionnaire de fermeture
     */
    private void setupShutdownHandler(Stage stage) {
        // Gestionnaire pour la fermeture de la fenêtre
        stage.setOnCloseRequest(event -> {
            logger.debug("Demande de fermeture de l'application");
            
            // Sauvegarde automatique si un jeu est en cours
            if (gameService != null && gameService.isGameRunning()) {
                try {
                    gameService.stopGame();
                    logger.info("Jeu sauvegardé avant fermeture");
                } catch (Exception e) {
                    logger.error("Erreur lors de la sauvegarde", e);
                }
            }
            
            // Nettoyage des ressources
            cleanup();
        });
        
        // Gestionnaire pour l'arrêt de la JVM
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.debug("Hook d'arrêt exécuté");
            cleanup();
        }));
    }
    
    /**
     * Nettoyage des ressources de l'application
     */
    private void cleanup() {
        logger.debug("Nettoyage des ressources...");
        
        try {
            // Arrêt du service de jeu
            if (gameService != null) {
                gameService.shutdown();
            }
            
            // Fermeture du service de stockage
            if (storageService != null) {
                storageService.close();
            }
            
            logger.debug("Ressources nettoyées");
            
        } catch (Exception e) {
            logger.error("Erreur lors du nettoyage", e);
        }
    }
    
    /**
     * Arrêt de l'application (appelée après la fermeture de toutes les fenêtres)
     */
    @Override
    public void stop() throws Exception {
        logger.info("Arrêt de l'application");
        cleanup();
        super.stop();
    }
    
    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void showErrorDialog(String title, String message, Exception exception) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(message);
            
            if (exception != null) {
                alert.setContentText(exception.getMessage());
                
                // Détails de l'exception
                var textArea = new javafx.scene.control.TextArea();
                textArea.setText(getStackTrace(exception));
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                
                var expandableContent = new javafx.scene.control.ScrollPane(textArea);
                alert.getDialogPane().setExpandableContent(expandableContent);
            }
            
            alert.showAndWait();
        });
    }
    
    /**
     * Obtient la stack trace d'une exception sous forme de chaîne
     */
    private String getStackTrace(Exception e) {
        var sw = new java.io.StringWriter();
        var pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Classe utilitaire pour les informations de l'application
     */
    public static class AppInfo {
        public static String getName() { return APP_TITLE; }
        public static String getVersion() { return APP_VERSION; }
        public static String getJavaVersion() { return System.getProperty("java.version"); }
        public static String getJavaFXVersion() { return System.getProperty("javafx.version"); }
        
        public static String getFullVersionString() {
            return String.format("%s v%s (Java %s, JavaFX %s)", 
                               APP_TITLE, APP_VERSION, 
                               getJavaVersion(), getJavaFXVersion());
        }
    }
    
    /**
     * Enum pour les thèmes de l'application
     */
    public enum Theme {
        LIGHT("Light Theme", "/css/light-theme.css"),
        DARK("Dark Theme", "/css/dark-theme.css"),
        SYSTEM("System Theme", null);
        
        private final String displayName;
        private final String cssFile;
        
        Theme(String displayName, String cssFile) {
            this.displayName = displayName;
            this.cssFile = cssFile;
        }
        
        public String getDisplayName() { return displayName; }
        public String getCssFile() { return cssFile; }
    }
    
    /**
     * Configuration de l'application (peut être étendue avec des préférences utilisateur)
     */
    public static class AppConfig {
        private static Theme currentTheme = Theme.SYSTEM;
        private static boolean enableAnimations = true;
        private static boolean enableSound = true;
        private static double masterVolume = 0.7;
        
        // Getters et setters
        public static Theme getCurrentTheme() { return currentTheme; }
        public static void setCurrentTheme(Theme theme) { currentTheme = theme; }
        
        public static boolean isAnimationsEnabled() { return enableAnimations; }
        public static void setAnimationsEnabled(boolean enabled) { enableAnimations = enabled; }
        
        public static boolean isSoundEnabled() { return enableSound; }
        public static void setSoundEnabled(boolean enabled) { enableSound = enabled; }
        
        public static double getMasterVolume() { return masterVolume; }
        public static void setMasterVolume(double volume) { 
            masterVolume = Math.max(0.0, Math.min(1.0, volume)); 
        }
    }
    
    /**
     * Factory method pour créer l'application avec une configuration spécifique
     */
    public static JProgressQuestApplication createWithConfig(StorageService storage, GameService game) {
        var app = new JProgressQuestApplication();
        app.storageService = storage;
        app.gameService = game;
        return app;
    }
    
    // Getters pour les services (utiles pour les tests ou l'intégration)
    public StorageService getStorageService() { return storageService; }
    public GameService getGameService() { return gameService; }
    public Stage getPrimaryStage() { return primaryStage; }
}