# JProgressQuest - Structure du projet Maven

## Structure des répertoires
```
JProgressQuest/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── progressquest/
│   │   │           ├── JProgressQuestApplication.java
│   │   │           ├── controller/
│   │   │           │   ├── MainController.java
│   │   │           │   ├── RosterController.java
│   │   │           │   └── NewCharacterController.java
│   │   │           ├── model/
│   │   │           │   ├── Game.java
│   │   │           │   ├── Character.java
│   │   │           │   ├── Stats.java
│   │   │           │   ├── Equipment.java
│   │   │           │   ├── Inventory.java
│   │   │           │   ├── ProgressBar.java
│   │   │           │   └── Constants.java
│   │   │           ├── service/
│   │   │           │   ├── GameService.java
│   │   │           │   ├── StorageService.java
│   │   │           │   ├── RandomService.java
│   │   │           │   └── NameGenerator.java
│   │   │           └── util/
│   │   │               ├── GameTimer.java
│   │   │               ├── StringUtils.java
│   │   │               └── MathUtils.java
│   │   └── resources/
│   │       ├── fxml/
│   │       │   ├── main-view.fxml
│   │       │   ├── roster-view.fxml
│   │       │   └── new-character-view.fxml
│   │       ├── css/
│   │       │   └── application.css
│   │       └── images/
│   └── test/
│       └── java/
│           └── com/
│               └── progressquest/
└── README.md
```

## Fichiers principaux à créer

1. **pom.xml** - Configuration Maven avec JavaFX 17
2. **JProgressQuestApplication.java** - Point d'entrée de l'application
3. **Model classes** - Représentation des données du jeu
4. **Service classes** - Logique métier et utilitaires
5. **Controller classes** - Contrôleurs JavaFX pour l'interface
6. **FXML files** - Définition des interfaces utilisateur
7. **Utility classes** - Fonctions utilitaires converties du JS

## Technologies utilisées
- **Java 17** - Version LTS avec nouvelles fonctionnalités
- **JavaFX 17** - Interface utilisateur moderne
- **Maven** - Gestion des dépendances et build
- **Jackson** - Sérialisation JSON pour la sauvegarde
- **JUnit 5** - Tests unitaires

## Fonctionnalités à implémenter
1. Génération de personnages aléatoires
2. Système de progression automatique
3. Gestion de l'équipement et inventaire
4. Système de quêtes et d'actes
5. Sauvegarde/chargement des parties
6. Interface utilisateur responsive