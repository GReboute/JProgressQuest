package com.JProgressQuest.controller;

import com.JProgressQuest.model.Constants;
import com.JProgressQuest.model.Game;
import com.JProgressQuest.service.GameService;
import com.JProgressQuest.service.StorageService;
import com.JProgressQuest.util.StringUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Contrôleur principal pour l'interface de jeu.
 * Utilise les fonctionnalités modernes de JavaFX et les bindings pour une interface réactive.
 */
public class MainController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    // Services injectés
    private final GameService gameService;
    private final StorageService storageService;
    
    // Stage principal
    private Stage stage;
    
    // === FXML Injections - Character Sheet ===
    @FXML private Label characterName;
    @FXML private Label characterRace;
    @FXML private Label characterClass;
    @FXML private Label characterLevel;
    
    @FXML private Label strStat;
    @FXML private Label conStat;
    @FXML private Label dexStat;
    @FXML private Label intStat;
    @FXML private Label wisStat;
    @FXML private Label chaStat;
    @FXML private Label hpMaxStat;
    @FXML private Label mpMaxStat;
    
    // === FXML Injections - Progress Bars ===
    @FXML private ProgressBar experienceBar;
    @FXML private Label experienceLabel;
    
    @FXML private ProgressBar encumbranceBar;
    @FXML private Label encumbranceLabel;
    
    @FXML private ProgressBar plotBar;
    @FXML private Label plotLabel;
    
    @FXML private ProgressBar questBar;
    @FXML private Label questLabel;
    
    @FXML private ProgressBar taskBar;
    @FXML private Label taskLabel;
    
    // === FXML Injections - Equipment ===
    @FXML private TableView<EquipmentEntry> equipmentTable;
    @FXML private TableColumn<EquipmentEntry, String> equipmentSlotColumn;
    @FXML private TableColumn<EquipmentEntry, String> equipmentItemColumn;
            
    // === FXML Injections - Inventory ===
    @FXML private TableView<InventoryEntry> inventoryTable;
    @FXML private TableColumn<InventoryEntry, String> inventoryItemColumn;
    @FXML private TableColumn<InventoryEntry, String> inventoryQuantityColumn;
    
    // === FXML Injections - Spells ===
    @FXML private TableView<SpellEntry> spellTable;
    @FXML private TableColumn<SpellEntry, String> spellNameColumn;
    @FXML private TableColumn<SpellEntry, String> spellLevelColumn;
    
    // === FXML Injections - Quests and Plots ===
    @FXML private ListView<String> questList;
    @FXML private ListView<String> plotList;
    
    // === FXML Injections - Controls ===
    @FXML private Button playPauseButton;
    @FXML private Button saveButton;
    @FXML private Button loadButton;
    @FXML private Button newGameButton;
    @FXML private Button rosterButton;
    @FXML private Button settingsButton;
    
    // === FXML Injections - Status ===
    @FXML private Label currentTaskLabel;
    @FXML private Label gameStatusLabel;
    
    // Properties JavaFX pour les bindings réactifs
    private final BooleanProperty gameRunning = new SimpleBooleanProperty(false);
    private final StringProperty currentTask = new SimpleStringProperty("En attente...");
    private final StringProperty gameStatus = new SimpleStringProperty("Prêt");
    
    // Collections observables pour les TableView et ListView
    private final ObservableList<EquipmentEntry> equipmentList = FXCollections.observableArrayList();
    private final ObservableList<InventoryEntry> inventoryList = FXCollections.observableArrayList();
    private final ObservableList<SpellEntry> spellList = FXCollections.observableArrayList();
    private final ObservableList<String> questsList = FXCollections.observableArrayList();
    private final ObservableList<String> plotsList = FXCollections.observableArrayList();
    
    // Timeline pour la mise à jour de l'interface
    private Timeline uiUpdateTimeline;
    
    // Jeu actuel
    private Game currentGame;
    
    /**
     * Constructeur avec injection de dépendances
     */
    public MainController(GameService gameService, StorageService storageService) {
        this.gameService = Objects.requireNonNull(gameService);
        this.storageService = Objects.requireNonNull(storageService);
        
        // Configuration du listener d'événements de jeu
        this.gameService.setEventListener(this::handleGameEvent);
    }
    
    /**
     * Initialisation du contrôleur (appelée après injection FXML)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initialisation du contrôleur principal");
        
        // Configuration des tables et listes
        setupTables();
        
        // Configuration des bindings
        setupBindings();
        
        // Configuration des contrôles
        setupControls();
        
        // Démarrage du timer de mise à jour de l'interface
        startUIUpdateTimer();
    }
    
    /**
     * Configuration des TableView et ListView
     */
    private void setupTables() {
        // Configuration de la table d'équipement
        equipmentSlotColumn.setCellValueFactory(new PropertyValueFactory<>("slot"));
        equipmentItemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        equipmentTable.setItems(equipmentList);
        // Augmenter la hauteur de la table d'équipement
        equipmentTable.setPrefHeight(300);
        
        // Configuration de la table d'inventaire  
        inventoryItemColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        inventoryQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        inventoryTable.setItems(inventoryList);
        // Réduire la hauteur de la table d'inventaire
        inventoryTable.setPrefHeight(150);

        // Configuration de la table de sorts
        spellNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        spellLevelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        spellTable.setItems(spellList);
        
        // Configuration des listes
        questList.setItems(questsList);
        plotList.setItems(plotsList);
        
        // Style des tables pour un meilleur rendu
        equipmentTable.setRowFactory(tv -> {
            TableRow<EquipmentEntry> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null && !newItem.getItem().isEmpty()) {
                    row.getStyleClass().add("equipped-row");
                } else {
                    row.getStyleClass().remove("equipped-row");
                }
            });
            return row;
        });
    }
    
    /**
     * Configuration des bindings pour une interface réactive
     */
    private void setupBindings() {
        // Binding des labels de statut
        currentTaskLabel.textProperty().bind(currentTask);
        gameStatusLabel.textProperty().bind(gameStatus);
        
        // Binding du bouton play/pause
        playPauseButton.textProperty().bind(
            Bindings.when(gameRunning)
                .then("Pause")
                .otherwise("Play")
        );
        
        // Désactivation des contrôles pendant le chargement
        BooleanProperty loading = new SimpleBooleanProperty(false);
        saveButton.disableProperty().bind(gameRunning.not().or(loading));
        loadButton.disableProperty().bind(gameRunning.or(loading));
        newGameButton.disableProperty().bind(loading);
        rosterButton.disableProperty().bind(loading);
        
        logger.debug("Bindings configurés");
    }
    
    /**
     * Configuration des contrôles
     */
    private void setupControls() {
        // Tooltips pour une meilleure UX
        playPauseButton.setTooltip(new Tooltip("Démarre ou met en pause le jeu (P)"));
        saveButton.setTooltip(new Tooltip("Sauvegarde la partie en cours (Ctrl+S)"));
        loadButton.setTooltip(new Tooltip("Charge une partie sauvegardée (Ctrl+O)"));
        newGameButton.setTooltip(new Tooltip("Crée un nouveau personnage (Ctrl+N)"));
        
        // Configuration des barres de progression avec des couleurs
        setupProgressBars();
    }
    
    /**
     * Configuration des barres de progression
     */
    private void setupProgressBars() {
        // Styles CSS pour les barres de progression
        experienceBar.getStyleClass().add("experience-bar");
        encumbranceBar.getStyleClass().add("encumbrance-bar");
        plotBar.getStyleClass().add("plot-bar");
        questBar.getStyleClass().add("quest-bar");
        taskBar.getStyleClass().add("task-bar");
        
        // Initialisation des tooltips (sans binding car mis à jour manuellement)
        experienceBar.setTooltip(new Tooltip());
        encumbranceBar.setTooltip(new Tooltip());
        plotBar.setTooltip(new Tooltip());
        questBar.setTooltip(new Tooltip());
        taskBar.setTooltip(new Tooltip());
    }
    
    /**
     * Démarre le timer de mise à jour de l'interface
     */
    private void startUIUpdateTimer() {
        uiUpdateTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> updateUI()));
        uiUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        uiUpdateTimeline.play();
        
        logger.debug("Timer de mise à jour de l'interface démarré");
    }
    
    /**
     * Met à jour l'interface utilisateur
     */
    private void updateUI() {
        if (currentGame == null) return;
        
        // Mise à jour en arrière-plan pour éviter de bloquer l'UI
        Platform.runLater(() -> {
            updateCharacterInfo();
            updateProgressBars();
            updateCollections();
        });
    }

    /**
     * Définit le jeu en cours et met à jour l'interface.
     */
    public void setCurrentGame(Game game) {
        this.currentGame = game;
        updateUI();
        gameStatus.set("Prêt pour : " + game.getTrait("Name"));
    }
    
    /**
     * Met à jour les informations du personnage
     */
    private void updateCharacterInfo() {
        if (currentGame == null) return;
        
        // Mise à jour des traits
        characterName.setText((String) currentGame.getTrait("Name"));
        characterRace.setText((String) currentGame.getTrait("Race"));
        characterClass.setText((String) currentGame.getTrait("Class"));
        characterLevel.setText(currentGame.getTrait("Level").toString());
        
        // Mise à jour des statistiques
        strStat.setText(String.valueOf(currentGame.getStat("STR")));
        conStat.setText(String.valueOf(currentGame.getStat("CON")));
        dexStat.setText(String.valueOf(currentGame.getStat("DEX")));
        intStat.setText(String.valueOf(currentGame.getStat("INT")));
        wisStat.setText(String.valueOf(currentGame.getStat("WIS")));
        chaStat.setText(String.valueOf(currentGame.getStat("CHA")));
        hpMaxStat.setText(String.valueOf(currentGame.getStat("HP Max")));
        mpMaxStat.setText(String.valueOf(currentGame.getStat("MP Max")));
    }
    
    /**
     * Met à jour les barres de progression
     */
    private void updateProgressBars() {
        if (currentGame == null) return;
        
        // Barre d'expérience
        updateProgressBar(experienceBar, experienceLabel, currentGame.getExpBar(), 
                         "XP needed for next level");
        
        // Barre d'encombrement
        updateProgressBar(encumbranceBar, encumbranceLabel, currentGame.getEncumbranceBar(), 
                         "cubits");
        
        // Barre d'intrigue
        updateProgressBar(plotBar, plotLabel, currentGame.getPlotBar(), 
                         "remaining");
        
        // Barre de quête
        updateProgressBar(questBar, questLabel, currentGame.getQuestBar(), 
                         "% complete");
        
        // Barre de tâche
        updateProgressBar(taskBar, taskLabel, currentGame.getTaskBar(), 
                         "% complete");
        
        // Mise à jour de la tâche actuelle
        currentTask.set(currentGame.getCurrentTask());
    }
    
    /**
     * Utilitaire pour mettre à jour une barre de progression
     */
    private void updateProgressBar(ProgressBar bar, Label label, 
                                 Game.ProgressBarState state, String unit) {
        // Vérification de nullité pour la robustesse, au cas où les éléments FXML seraient manquants.
        if (bar == null || label == null || state == null) {
            return;
        }

        if (state.getMax() > 0) {
            double progress = (double) state.getPosition() / state.getMax();
            bar.setProgress(progress);
            
            // Création du template pour l'affichage
            Map<String, Object> templateData = Map.of(
                "position", state.getPosition(),
                "max", state.getMax(),
                "percent", state.getPercent(),
                "remaining", state.getRemaining(),
                "time", StringUtils.formatTime(state.getRemaining() / 1000)
            );
            
            String labelText = StringUtils.applyTemplate(
                "$position/$max ($percent%)", templateData);
            label.setText(labelText);
            
            // Mise à jour du tooltip si présent
            if (bar.getTooltip() != null) {
                String tooltipText = StringUtils.applyTemplate(
                    "$remaining " + unit + " ($time remaining)", templateData);
                bar.getTooltip().setText(tooltipText);
            }
        }
    }
    
    /**
     * Met à jour les collections (tables et listes)
     */
    private void updateCollections() {
        if (currentGame == null) return;
        
        // Mise à jour de l'équipement
        equipmentList.clear();
        currentGame.getEquipment().forEach((slot, item) -> 
            equipmentList.add(new EquipmentEntry(slot, item))
        );
        
        // Mise à jour de l'inventaire
        inventoryList.clear();
        currentGame.getInventory().forEach(item -> 
            inventoryList.add(new InventoryEntry(item.name(), item.quantity()))
        );
        
        // Mise à jour des sorts
        spellList.clear();
        currentGame.getSpells().forEach(spell -> 
            spellList.add(new SpellEntry(spell.name(), spell.level()))
        );
        
        // Mise à jour des quêtes et intrigues
        questsList.setAll(currentGame.getQuests());
        plotsList.setAll(currentGame.getPlots());
    }
    
    // === Event Handlers ===
    
    /**
     * Gère le clic sur le bouton Play/Pause
     */
    @FXML
    private void handlePlayPauseAction() {
        if (currentGame == null) {
            showInfoAlert("Aucun jeu", "Veuillez charger ou créer un nouveau jeu.");
            return;
        }
        
        if (gameService.isGameRunning()) {
            gameService.stopGame();
            gameRunning.set(false);
            gameStatus.set("En pause");
            logger.info("Jeu mis en pause");
        } else {
            gameService.startGame(currentGame);
            gameRunning.set(true);
            gameStatus.set("En cours");
            logger.info("Jeu démarré");
        }
    }
    
    /**
     * Gère la sauvegarde
     */
    @FXML
    private void handleSaveAction() {
        if (currentGame == null) {
            showInfoAlert("Aucun jeu", "Aucun jeu à sauvegarder.");
            return;
        }
        
        // Sauvegarde asynchrone pour ne pas bloquer l'UI
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Sauvegarde en cours...");
                storageService.saveGame(currentGame);
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    gameStatus.set("Jeu sauvegardé");
                    showInfoAlert("Sauvegarde", "Jeu sauvegardé avec succès.");
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    Throwable exception = getException();
                    logger.error("Erreur de sauvegarde", exception);
                    showErrorAlert("Erreur de sauvegarde", 
                                 "Impossible de sauvegarder le jeu.", exception);
                });
            }
        };
        
        // Binding du statut avec le progrès de la tâche
        gameStatus.bind(saveTask.messageProperty());
        saveTask.setOnSucceeded(e -> gameStatus.unbind());
        saveTask.setOnFailed(e -> gameStatus.unbind());
        
        // Exécution en arrière-plan
        Thread saveThread = new Thread(saveTask);
        saveThread.setDaemon(true);
        saveThread.start();
    }
    
    /**
     * Gère le chargement d'un jeu
     */
    @FXML
    private void handleLoadAction() {
        // Affichage d'une liste des jeux sauvegardés
        showGameSelectionDialog().ifPresent(this::loadGame);
    }
    
    /**
     * Gère la création d'un nouveau jeu
     */
    @FXML
    private void handleNewGameAction() {
        // Arrêt du jeu actuel s'il y en a un
        if (gameService.isGameRunning()) {
            gameService.stopGame();
            gameRunning.set(false);
        }
        
        // Affichage du dialogue de création de personnage
        showCharacterCreationDialog().ifPresent(this::startNewGame);
    }

    /**
     * Gère l'importation d'un fichier .pqw externe
     */
    @FXML
    private void handleImportPqwAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer un personnage Progress Quest (.pqw)");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers Progress Quest", "*.pqw")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                Game imported = storageService.importPqwFile(selectedFile.toPath());
                setCurrentGame(imported);
                showInfoAlert("Importation réussie", 
                    "Le personnage '" + imported.getTrait("Name") + "' a été importé et ajouté au roster.");
            } catch (Exception e) {
                logger.error("Erreur lors de l'importation du fichier .pqw", e);
                showErrorAlert("Erreur d'importation", "Impossible de lire ou de convertir le fichier .pqw.", e);
            }
        }
    }

    /**
     * Gère l'affichage des paramètres
     */
    @FXML
    private void handleSettingsAction() {
        // Crée un dialogue personnalisé pour les paramètres
        try {
            Dialog<Pair<String, GameService.GameSpeed>> dialog = new Dialog<>();
            dialog.setTitle("Paramètres");
            dialog.setHeaderText("Réglez les paramètres de l'application.");

            // Ajout des boutons
            ButtonType applyButtonType = new ButtonType("Appliquer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

            // Création de la grille pour les contrôles
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Contrôle du niveau de log
            ch.qos.logback.classic.LoggerContext loggerContext = (ch.qos.logback.classic.LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger appLogger = loggerContext.getLogger("com.JProgressQuest");
            ch.qos.logback.classic.Level currentLevel = appLogger.getEffectiveLevel();
            String defaultLogChoice = (currentLevel != null) ? currentLevel.toString() : "WARN";
            ChoiceBox<String> logLevelBox = new ChoiceBox<>(FXCollections.observableArrayList("TRACE", "DEBUG", "INFO", "WARN", "ERROR"));
            logLevelBox.setValue(defaultLogChoice);

            // Contrôle de la vitesse du jeu
            ChoiceBox<GameService.GameSpeed> gameSpeedBox = new ChoiceBox<>(FXCollections.observableArrayList(GameService.GameSpeed.values()));
            gameSpeedBox.setValue(gameService.getGameSpeed());

            grid.add(new Label("Niveau de Log:"), 0, 0);
            grid.add(logLevelBox, 1, 0);
            grid.add(new Label("Vitesse du jeu:"), 0, 1);
            grid.add(gameSpeedBox, 1, 1);

            dialog.getDialogPane().setContent(grid);

            // Conversion du résultat en une paire de valeurs
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == applyButtonType) {
                    return new Pair<>(logLevelBox.getValue(), gameSpeedBox.getValue());
                }
                return null;
            });

            Optional<Pair<String, GameService.GameSpeed>> result = dialog.showAndWait();

            result.ifPresent(settings -> {
                // Appliquer le niveau de log
                ch.qos.logback.classic.Level newLevel = ch.qos.logback.classic.Level.toLevel(settings.getKey());
                if (appLogger.getLevel() != newLevel) {
                    appLogger.setLevel(newLevel);
                    logger.info("Niveau de log modifié vers: {}", newLevel);
                }

                // Appliquer la vitesse du jeu
                gameService.setGameSpeed(settings.getValue());
            });
        } catch (ClassCastException e) {
            logger.warn("Impossible de changer le niveau de log : Logback n'est pas l'implémentation sous-jacente.");
            showInfoAlert("Paramètres", "Impossible de changer la configuration de log (Logback requis).");
        }
    }
    
    /**
     * Gère le retour au roster (menu principal)
     */
    @FXML
    private void handleBackToRosterAction() {
        // Arrêt propre du jeu actuel
        if (gameService.isGameRunning()) {
            gameService.stopGame();
            gameRunning.set(false);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/roster-view.fxml"));
            loader.setControllerFactory(type -> {
                if (type == RosterController.class) return new RosterController(gameService, storageService);
                return null;
            });
            
            Scene scene = new Scene(loader.load());
            RosterController controller = loader.getController();
            controller.setStage(stage);
            
            URL css = getClass().getResource("/css/application.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            
            stage.setScene(scene);
            logger.info("Retour au menu principal (Roster)");
        } catch (IOException e) {
            logger.error("Erreur lors du retour au roster", e);
            showErrorAlert("Erreur", "Impossible de revenir au menu principal.", e);
        }
    }
    
    // === Méthodes utilitaires ===
    
    /**
     * Charge un jeu sélectionné
     */
    private void loadGame(String characterName) {
        // Chargement asynchrone
        CompletableFuture
            .supplyAsync(() -> {
                try {
                    return storageService.loadGame(characterName);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .thenAcceptAsync(game -> Platform.runLater(() -> {
                currentGame = game;
                gameStatus.set("Jeu chargé: " + characterName);
                updateUI();
                logger.info("Jeu chargé: {}", characterName);
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    logger.error("Erreur de chargement", throwable);
                    showErrorAlert("Erreur de chargement", 
                                 "Impossible de charger le jeu: " + characterName, 
                                 (Exception) throwable.getCause());
                });
                return null;
            });
    }
    
    /**
     * Démarre un nouveau jeu avec les paramètres donnés
     */
    private void startNewGame(CharacterCreationData data) {
        currentGame = gameService.createNewCharacter(data.name(), data.race(), data.characterClass());
        gameStatus.set("Nouveau jeu créé: " + data.name());
        updateUI();
        logger.info("Nouveau jeu créé: {}", data.name());
    }
    
    /**
     * Gère les événements de jeu
     */
    private void handleGameEvent(Constants.GameEvent event) {
        Platform.runLater(() -> {
            switch (event) {
                case Constants.GameEvent.LevelUp levelUp -> {
                    gameStatus.set("Niveau " + levelUp.newLevel() + " atteint!");
                    // Animation ou effet visuel possible ici
                }
                case Constants.GameEvent.QuestComplete questComplete -> {
                    gameStatus.set("Quête terminée: " + questComplete.questName());
                }
                case Constants.GameEvent.ItemFound itemFound -> {
                    gameStatus.set("Objet trouvé: " + itemFound.itemName());
                }
                case Constants.GameEvent.EquipmentBought equipmentBought -> {
                    gameStatus.set(String.format("Achat: %s (%d or) sur %s", equipmentBought.itemName(), equipmentBought.cost(), equipmentBought.slot()));
                }
            }
        });
    }
    
    /**
     * Affiche un dialogue de sélection de jeu
     */
    private java.util.Optional<String> showGameSelectionDialog() {
        try {
            var games = storageService.listGames();
            
            if (games.isEmpty()) {
                showInfoAlert("Aucun jeu", "Aucun jeu sauvegardé trouvé.");
                return java.util.Optional.empty();
            }
            
            ChoiceDialog<StorageService.GameSummary> dialog = new ChoiceDialog<>(games.get(0), games);
            dialog.setTitle("Charger un jeu");
            dialog.setHeaderText("Sélectionnez un jeu à charger:");
            dialog.setContentText("Jeu:");
            
            // Configuration de l'affichage des éléments
            dialog.getDialogPane().lookup(".combo-box").setStyle("-fx-pref-width: 300px;");
            
            return dialog.showAndWait().map(StorageService.GameSummary::name);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des jeux", e);
            showErrorAlert("Erreur", "Impossible de récupérer la liste des jeux.", e);
            return java.util.Optional.empty();
        }
    }
    
    /**
     * Affiche un dialogue de création de personnage
     */
    private java.util.Optional<CharacterCreationData> showCharacterCreationDialog() {
        // TODO: Implémenter un dialogue complet de création de personnage
        // Pour l'instant, utilise des dialogues simples
        
        TextInputDialog nameDialog = new TextInputDialog("Héros");
        nameDialog.setTitle("Nouveau personnage");
        nameDialog.setHeaderText("Création d'un nouveau personnage");
        nameDialog.setContentText("Nom du personnage:");
        
        return nameDialog.showAndWait().map(name -> 
            new CharacterCreationData(name, "Human", "Fighter") // Valeurs par défaut
        );
    }
    
    /**
     * Affiche une alerte d'information
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte d'erreur
     */
    private void showErrorAlert(String title, String message, Throwable exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        
        if (exception != null) {
            alert.setContentText(exception.getMessage());
            
            // Détails de l'exception
            TextArea textArea = new TextArea(getStackTrace(exception));
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            
            alert.getDialogPane().setExpandableContent(new ScrollPane(textArea));
        }
        
        alert.showAndWait();
    }
    
    private String getStackTrace(Throwable e) {
        var sw = new java.io.StringWriter();
        var pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    // === Records pour les données d'affichage ===
    
    /**
     * Record pour les entrées d'équipement dans la TableView
     */
    public record EquipmentEntry(String slot, String item) {
        public String getSlot() { return slot; }
        public String getItem() { return item; }
    }
    
    /**
     * Record pour les entrées d'inventaire dans la TableView
     */
    public record InventoryEntry(String name, Integer quantity) {
        public String getName() { return name; }
        public Integer getQuantity() { return quantity; }
    }
    
    /**
     * Record pour les entrées de sorts dans la TableView
     */
    public record SpellEntry(String name, String level) {
        public String getName() { return name; }
        public String getLevel() { return level; }
    }
    
    /**
     * Record pour les données de création de personnage
     */
    public record CharacterCreationData(String name, String race, String characterClass) {}
    
    // === Setters pour l'injection ===
    
    public void setStage(Stage stage) {
        this.stage = stage;
        
        // Configuration des raccourcis clavier
        setupKeyboardShortcuts();
    }
    
    /**
     * Configuration des raccourcis clavier
     */
    private void setupKeyboardShortcuts() {
        if (stage != null && stage.getScene() != null) {
            stage.getScene().setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case P -> {
                        if (!event.isControlDown()) {
                            handlePlayPauseAction();
                            event.consume();
                        }
                    }
                    case S -> {
                        if (event.isControlDown()) {
                            handleSaveAction();
                            event.consume();
                        }
                    }
                    case O -> {
                        if (event.isControlDown()) {
                            handleLoadAction();
                            event.consume();
                        }
                    }
                    case N -> {
                        if (event.isControlDown()) {
                            handleNewGameAction();
                            event.consume();
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Nettoyage des ressources lors de la fermeture
     */
    public void cleanup() {
        if (uiUpdateTimeline != null) {
            uiUpdateTimeline.stop();
        }
        
        if (gameService.isGameRunning()) {
            gameService.stopGame();
        }
    }
}
