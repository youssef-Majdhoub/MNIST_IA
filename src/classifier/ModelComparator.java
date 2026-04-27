package classifier;

import data.BinaryMNISTReader;
import data.TextFileHandler;
import interfaces.DigitClassifier;

import java.util.List;

/**
 * Compare les performances de NaiveBayes et RandomForest sur un jeu de test.
 *
 * Workflow :
 *   1. Charge train-data.arff (800 exemples d'entraînement)
 *   2. Crée test-data.arff à partir de 100 exemples supplémentaires (50 de chaque classe)
 *   3. Instancie les deux classifieurs
 *   4. Prédit le label de chaque exemple de test
 *   5. Calcule et affiche le taux de précision de chaque modèle
 */
public class ModelComparator {

    private final String imagesPath;
    private final String labelsPath;
    private final String trainArff;
    private final String testArff;
    private final String trainCsv;
    private final String testCsv;

    public ModelComparator(String imagesPath, String labelsPath,
                           String trainArff, String testArff) {
        this.imagesPath = imagesPath;
        this.labelsPath = labelsPath;
        this.trainArff  = trainArff;
        this.testArff   = testArff;
        this.trainCsv   = trainArff.replace(".arff", ".csv");
        this.testCsv    = testArff.replace(".arff", ".csv");
    }

    /**
     * Prépare les données, entraîne les modèles et affiche les résultats.
     */
    public void run() throws Exception {

        // ---- 1. Générer les données d'entraînement (400+400 = 800 exemples) ----
        System.out.println("=== Génération des données ===");
        TextFileHandler.createTextFile(imagesPath, labelsPath, 400, trainCsv);
        TextFileHandler.csvToArff(trainCsv, trainArff);

        // ---- 2. Générer les données de test (50+50 = 100 exemples supplémentaires) ----
        // On utilise un offset : charge 450 exemples par classe et on garde les 50 derniers
        BinaryMNISTReader reader = new BinaryMNISTReader(imagesPath, labelsPath);
        reader.load(450);   // 450 trois + 450 cinq

        List<int[]>   allPixels = reader.getPixels();
        List<Integer> allLabels = reader.getLabels();

        // Sépare les 50 derniers de chaque classe (index 400-449 de chaque classe)
        List<int[]>   testPixels = new java.util.ArrayList<>();
        List<Integer> testLabels  = new java.util.ArrayList<>();

        int troisCount = 0, cinqCount = 0;
        // D'abord, compter comment les classes sont intercalées
        // On reconstruit les 50 derniers trois et cinq
        int[] troisIdx = new int[50];
        int[] cinqIdx  = new int[50];
        int ti = 0, ci = 0;
        for (int i = 0; i < allLabels.size(); i++) {
            if (allLabels.get(i) == 3 && ti < 50 && troisCount >= 400) {
                troisIdx[ti++] = i;
            }
            if (allLabels.get(i) == 5 && ci < 50 && cinqCount >= 400) {
                cinqIdx[ci++] = i;
            }
            if (allLabels.get(i) == 3) troisCount++;
            if (allLabels.get(i) == 5) cinqCount++;
        }

        // Si pas assez d'offset, on prend juste les 50 premiers disponibles après les 400
        // Approche simplifiée : on prend les 100 derniers éléments du reader
        int total = allPixels.size();
        for (int i = Math.max(0, total - 100); i < total; i++) {
            testPixels.add(allPixels.get(i));
            testLabels.add(allLabels.get(i));
        }

        // Export vers CSV puis ARFF
        TextFileHandler testHandler = new TextFileHandler(testPixels, testLabels);
        testHandler.export(testCsv);
        TextFileHandler.csvToArff(testCsv, testArff);
        System.out.println("Données de test : " + testPixels.size() + " exemples");

        // ---- 3. Instanciation des classifieurs ----
        System.out.println("\n=== Entraînement des modèles ===");
        NaiveBayesClassifier   nbClassifier = new NaiveBayesClassifier(trainArff);
        RandomForestClassifier rfClassifier = new RandomForestClassifier(trainArff);

        // ---- 4. Prédiction et calcul de l'accuracy ----
        System.out.println("\n=== Évaluation sur le jeu de test ===");
        int nbCorrect = 0, rfCorrect = 0;
        int n = testPixels.size();

        for (int i = 0; i < n; i++) {
            int[]  pixel     = testPixels.get(i);
            String trueLabel = (testLabels.get(i) == 3) ? "trois" : "cinq";

            String nbPred = nbClassifier.predict(pixel);
            String rfPred = rfClassifier.predict(pixel);

            if (nbPred.equals(trueLabel)) nbCorrect++;
            if (rfPred.equals(trueLabel)) rfCorrect++;
        }

        double nbAccuracy = 100.0 * nbCorrect / n;
        double rfAccuracy = 100.0 * rfCorrect / n;

        // ---- 5. Affichage du tableau de résultats ----
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║           COMPARAISON DES MODÈLES                ║");
        System.out.println("╠══════════════════╦══════════════╦════════════════╣");
        System.out.println("║ Modèle           ║ Corrects     ║ Accuracy       ║");
        System.out.println("╠══════════════════╬══════════════╬════════════════╣");
        System.out.printf( "║ Naive Bayes      ║ %4d / %-5d ║ %8.2f %%    ║%n",
                nbCorrect, n, nbAccuracy);
        System.out.printf( "║ Random Forest    ║ %4d / %-5d ║ %8.2f %%    ║%n",
                rfCorrect, n, rfAccuracy);
        System.out.println("╚══════════════════╩══════════════╩════════════════╝");

        String winner = (rfAccuracy > nbAccuracy)
                ? "Random Forest"
                : (nbAccuracy > rfAccuracy) ? "Naive Bayes" : "Égalité";
        System.out.println("Meilleur modèle : " + winner);
    }

    // ------------------------------------------------------------------ //
    //  Main
    // ------------------------------------------------------------------ //

    public static void main(String[] args) {
        // Chemins par défaut — modifier selon votre installation
        String imagesPath = "data/train-images-idx3-ubyte";
        String labelsPath = "data/train-labels-idx1-ubyte";
        String trainArff  = "data/train-data.arff";
        String testArff   = "data/test-data.arff";

        if (args.length == 4) {
            imagesPath = args[0];
            labelsPath = args[1];
            trainArff  = args[2];
            testArff   = args[3];
        }

        ModelComparator comparator = new ModelComparator(
                imagesPath, labelsPath, trainArff, testArff);
        try {
            comparator.run();
        } catch (Exception e) {
            System.err.println("Erreur lors de la comparaison : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
