# JProgressQuest

Une conversion Java 23 + JavaFX du célèbre jeu satirique "Progress Quest" originalement développé en JavaScript.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-17-blue?style=flat-square)
![Maven](https://img.shields.io/badge/Maven-3.8+-green?style=flat-square&logo=apache-maven)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

## 📖 Description

JProgressQuest est un "zero-player game" où vous créez un personnage RPG qui progresse automatiquement sans aucune intervention du joueur. Le jeu parodie les mécaniques des MMORPG traditionnels en automatisant complètement l'expérience de jeu.

### Fonctionnalités

- ✨ **Génération automatique** de personnages avec races et classes
- 🎯 **Progression automatique** avec combats, quêtes et montées de niveau
- 🎒 **Système d'équipement** et d'inventaire dynamique
- 📜 **Système de sorts** avec progression
- 🏆 **Quêtes procédurales** et développement d'intrigue
- 💾 **Sauvegarde/chargement** avec gestion des sauvegardes multiples
- 🎨 **Interface moderne** avec JavaFX et CSS personnalisé
- 🌓 **Support thème sombre** et options d'accessibilité

## 🚀 Démarrage rapide

### Prérequis

- **Java 23** ou supérieur (LTS recommandé)
- **Maven 3.8** ou supérieur
- **JavaFX 17** (inclus dans les dépendances Maven)

### Installation

```bash
# Cloner le repository
git clone https://github.com/GReboute/JProgressQuest.git
cd JProgressQuest

# Compilation avec Maven
mvn clean compile

# Exécution de l'application
mvn javafx:run

# Ou création d'un JAR exécutable
mvn clean package
java -jar target/jprogressquest-1.0.0.jar
```

### Premier lancement

1. Cliquez sur **"Nouveau Jeu"** pour créer votre premier personnage
2. Entrez un nom pour votre héros
3. Cliquez sur **"Play"** pour commencer l'aventure automatique
4. Regardez votre personnage progresser sans rien faire !

## 🏗️ Architecture

Le projet utilise une architecture moderne Java 17+ avec les patterns suivants :

### Structure du projet

```
src/main/java/com/progressquest/
├── JProgressQuestApplication.java      # Point d'entrée
├── controller/                         # Contrôleurs JavaFX
│   ├── MainController.java
│   ├── RosterController.java
│   └── NewCharacterController.java
├── model/                             # Modèles de données
│   ├── Game.java                      # État principal du jeu
│   ├── Constants.java                 # Constantes du jeu
│   └── Character.java
├── service/                           # Logique métier
│   ├── GameService.java               # Moteur de jeu principal
│   ├── StorageService.java            # Sauvegarde/chargement
│   ├── RandomService.java             # Générateur aléatoire
│   └── NameGenerator.java             # Génération de noms
└── util/                             # Utilitaires
    ├── StringUtils.java               # Manipulation de chaînes
    └── MathUtils.java                 # Utilitaires mathématiques
```

### Technologies utilisées

- **Java 23** : Records, Pattern Matching, Text Blocks, Sealed Classes
- **JavaFX 17** : Interface utilisateur moderne et réactive
- **Jackson** : Sérialisation JSON pour les sauvegardes
- **SLF4J + Logback** : Système de logging professionnel
- **JUnit 5** : Tests unitaires avec les dernières fonctionnalités
- **Maven** : Gestion des dépendances et build

##
