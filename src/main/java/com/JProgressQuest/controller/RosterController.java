package com.JProgressQuest.controller;

import com.JProgressQuest.model.Game;
import com.JProgressQuest.service.GameService;
import com.JProgressQuest.service.StorageService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Contrôleur pour l'écran de gestion des personnages (Roster).
 * Permet de charger, créer ou importer des personnages.
 */
public class RosterController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(RosterController.class);
    
    private final GameService gameService;
    private final StorageService storageService;
    private Stage stage;
    
    @FXML private ListView<StorageService.GameSummary> characterListView;
    @FXML private Button playButton;
    @FXML private Button deleteButton;
    
    public RosterController(GameService gameService, StorageService storageService) {
        this.gameService = Objects.requireNonNull(gameService);
        this.storageService = Objects.requireNonNull(storageService);
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupListView();
        loadCharacters();
        
        // Activation conditionnelle des boutons selon la sélection
        playButton.disableProperty().bind(characterListView.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(characterListView.getSelectionModel().selectedItemProperty().isNull());
    }
    
    private void setupListView() {
        characterListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(StorageService.GameSummary item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName() + " - " + item.getFormattedLastSaved());
                }
            }
        });
    }
    
    private void loadCharacters() {
        try {
            ObservableList<StorageService.GameSummary> games = FXCollections.observableArrayList(storageService.listGames());
            characterListView.setItems(games);
        } catch (IOException e) {
            logger.error("Erreur lors du chargement du roster", e);
        }
    }
    
    @FXML
    private void handlePlay() {
        StorageService.GameSummary selected = characterListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                Game game = storageService.loadGame(selected.name());
                switchToMainView(game);
            } catch (IOException e) {
                logger.error("Erreur de chargement", e);
            }
        }
    }
    
    @FXML
    private void handleNewGame() {
        TextInputDialog nameDialog = new TextInputDialog("Héros");
        nameDialog.setTitle("Nouveau personnage");
        nameDialog.setHeaderText("Commencer une nouvelle aventure");
        nameDialog.setContentText("Nom du personnage:");
        
        nameDialog.showAndWait().ifPresent(name -> {
            Game newGame = gameService.createNewCharacter(name, "Human", "Fighter");
            switchToMainView(newGame);
        });
    }
    
    @FXML
    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer une sauvegarde JSON");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try {
                Game imported = storageService.importGame(file.toPath());
                storageService.saveGame(imported); // On l'enregistre localement
                loadCharacters();
                showAlert("Importation", "Personnage importé avec succès : " + imported.getTrait("Name"));
            } catch (IOException e) {
                logger.error("Erreur d'importation", e);
            }
        }
    }

    @FXML
    private void handleImportPqw() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer un personnage Progress Quest (.pqw)");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers Progress Quest", "*.pqw")
        );
    }

    @FXML
    private void handleDelete() {
        StorageService.GameSummary selected = characterListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + selected.name() + " ?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        storageService.deleteGame(selected.name());
                        loadCharacters();
                    } catch (IOException e) {
                        logger.error("Erreur de suppression", e);
                    }
                }
            });
        }
    }
    
    private void switchToMainView(Game game) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            loader.setControllerFactory(type -> {
                if (type == MainController.class) return new MainController(gameService, storageService);
                return null;
            });
            
            Scene scene = new Scene(loader.load());
            MainController controller = loader.getController();
            controller.setStage(stage);
            controller.setCurrentGame(game);
            
            URL css = getClass().getResource("/css/application.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            
            stage.setScene(scene);
        } catch (IOException e) {
            logger.error("Erreur de transition vers le jeu", e);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}