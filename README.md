🖌️ Reconnaissance de Chiffres Manuscrits (MNIST) en Java
Ce projet est une application Java orientée objet avec interface graphique (Swing) permettant la reconnaissance de chiffres manuscrits (spécifiquement les 3 et les 5) en utilisant des algorithmes d'Intelligence Artificielle via la bibliothèque Weka.

Il intègre une architecture logicielle robuste couvrant le traitement de fichiers, la gestion d'exceptions personnalisées, le polymorphisme et une suite de tests automatisés complète.

✨ Fonctionnalités Principales
Interface Graphique Interactive : Un canevas de dessin (280x280) permettant à l'utilisateur de tracer un chiffre à la souris.

Intelligence Artificielle : Classification des dessins à l'aide de deux modèles de Machine Learning entraînés sur le dataset MNIST :

Naive Bayes

Random Forest

Traitement de Données Multi-formats : Lecture des fichiers binaires originaux MNIST (.idx-ubyte), conversion en CSV, export en Excel (.xlsx) et génération de fichiers ARFF pour Weka.

Suite de Tests Intégrée : Une classe TestComplet générant sa propre fenêtre de résultats pour valider dynamiquement plus de 30 points critiques (Architecture, Exceptions, ML, GUI).

🏗️ Architecture du Projet
Le code est rigoureusement découpé selon les principes de la Programmation Orientée Objet (POO) :

📁 gui/ : Contient l'interface graphique MNISTGui (Canevas de dessin, boutons, affichage des probabilités).

📁 classifier/ : Implémentation des modèles IA (NaiveBayesClassifier, RandomForestClassifier) encapsulant la logique Weka.

📁 data/ : Classes de traitement de fichiers (BinaryMNISTReader, TextFileHandler, ExcelExporter).

📁 exceptions/ : Exceptions métier personnalisées (ex: InvalidDimensionsException, MNISTFileNotFoundException).

📁 interfaces/ : Contrats garantissant le polymorphisme (DigitClassifier, DataProcessor).

📁 test/ : Scénarios de tests end-to-end (TestComplet).

⚙️ Prérequis
Pour compiler et exécuter ce projet, vous aurez besoin de :

Java Development Kit (JDK) 8 ou supérieur.

Weka API (weka.jar) : Pour les algorithmes de Machine Learning.

(Optionnel) Apache POI : Si votre ExcelExporter l'utilise pour générer des fichiers .xlsx.

Les données MNIST d'entraînement (à placer dans un dossier data/ à la racine du projet) :

train-images.idx3-ubyte

train-labels.idx1-ubyte

Note : Le programme générera automatiquement les fichiers .csv et .arff s'ils sont manquants lors des tests.

🚀 Comment lancer le projet
1. Lancer l'Application Principale (Interface Utilisateur)
Pour utiliser l'application de dessin et de reconnaissance :

Bash
# Assurez-vous d'inclure weka.jar dans votre classpath
java -cp ".:path/to/weka.jar" gui.MNISTGui
Note : Au lancement, l'application charge les modèles IA en arrière-plan (chargement asynchrone pour ne pas bloquer l'interface).

2. Lancer la Suite de Tests Globale
Pour vérifier l'intégrité de l'ensemble du code, lancez la classe TestComplet. Cela ouvrira l'interface graphique de test accompagnée de la fenêtre principale.

Bash
java -cp ".:path/to/weka.jar" test.TestComplet
Vous verrez une barre de progression et un terminal intégré validant en temps réel les accès fichiers, les exceptions, la précision des algorithmes (Accuracy > 50%) et les interactions graphiques simulées.

📸 Captures d'écran

Interface de dessin :[ ![Interface](lien_vers_image_interface.png)](https://github.com/youssef-Majdhoub/MNIST_IA/blob/master/capture_interface.png)


🛠️ Améliorations futures possibles
Étendre la reconnaissance à l'ensemble des 10 chiffres (0 à 9).

Lissage des traits de dessin (utilisation de drawLine avec BasicStroke au lieu de points isolés).

Ajout de nouveaux algorithmes de classification (ex: SVM, Réseaux de neurones multicouches).
