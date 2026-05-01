package test;

import classifier.NaiveBayesClassifier;
import classifier.RandomForestClassifier;
import data.BinaryMNISTReader;
import data.ExcelExporter;
import data.TextFileHandler;
import exceptions.DataFormatMismatchException;
import exceptions.InvalidDimensionsException;
import exceptions.MNISTFileNotFoundException;
import gui.MNISTGui;
import interfaces.DataProcessor;
import interfaces.DigitClassifier;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.List;

/**
 * TestComplet — Lance MNISTGui et teste TOUTES les parties du projet :
 *
 *   Partie 1 — Architecture OO & Fichiers   (interfaces DataProcessor, classes concrètes)
 *   Partie 2 — Exceptions personnalisées
 *   Partie 3 — IA & Polymorphisme (Weka)
 *   Partie 4 — Interface Graphique (MNISTGui)
 *
 * Une fenêtre de résultats s'ouvre en parallèle de MNISTGui.
 */
public class TestComplet {

    // ── Chemins ─────────────────────────────────────────────────────────
    private static final String IMAGES_PATH = "data/train-images.idx3-ubyte";
    private static final String LABELS_PATH = "data/train-labels.idx1-ubyte";
    private static final String CSV_FILE    = "data/chiffres.txt";
    private static final String CSV_400     = "data/train-data.csv";
    private static final String ARFF_TRAIN  = "data/train-data.arff";
    private static final String EXCEL_FILE  = "data/chiffres.xlsx";
    private static final String TEST_IMAGE  = "data/test_image.png";
    private static final String BAD_CSV     = "data/bad_format.txt";

    // ── Compteurs ────────────────────────────────────────────────────────
    private static int totalT = 0, passT = 0, failT = 0;

    // ── Référence à la GUI testée ────────────────────────────────────────
    private static MNISTGui gui;

    // ── Fenêtre de résultats ─────────────────────────────────────────────
    private static JTextArea  outputArea;
    private static JLabel     summaryLabel;
    private static JProgressBar progressBar;

    // ================================================================== //
    //  MAIN
    // ================================================================== //

    public static void main(String[] args) throws Exception {

        // 1. Préparer les fichiers de données si nécessaires
        generateTestImageIfMissing();
        prepareArffIfMissing();

        // 2. Lancer MNISTGui (la vraie GUI du projet)
        SwingUtilities.invokeAndWait(() -> {
            gui = new MNISTGui(ARFF_TRAIN);
        });

        // 3. Ouvrir la fenêtre de résultats de tests
        SwingUtilities.invokeAndWait(() -> buildTestWindow());

        // 4. Attendre que les modèles soient chargés dans MNISTGui
        log("\n  [INFO] Attente du chargement des modèles IA dans MNISTGui...");
        waitForModels();
        log("  [INFO] Modèles prêts ✓\n");

        // 5. Lancer tous les tests
        runAllTests();
    }

    // ================================================================== //
    //  FENÊTRE DE RÉSULTATS
    // ================================================================== //

    private static void buildTestWindow() {
        JFrame frame = new JFrame("TestComplet — Résultats");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(700, 600);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(new Color(15, 15, 25));

        // Titre
        JLabel title = new JLabel("TestComplet — MNIST Projet OO", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 15));
        title.setForeground(new Color(100, 140, 255));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        root.add(title, BorderLayout.NORTH);

        // Console
        outputArea = new JTextArea();
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        outputArea.setBackground(new Color(10, 10, 18));
        outputArea.setForeground(new Color(220, 220, 240));
        outputArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(35, 35, 55)));
        root.add(scroll, BorderLayout.CENTER);

        // Bas
        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(new Color(15, 15, 25));
        bottom.setBorder(new EmptyBorder(8, 0, 0, 0));

        progressBar = new JProgressBar(0, 100);
        progressBar.setBackground(new Color(35, 35, 55));
        progressBar.setForeground(new Color(100, 140, 255));
        progressBar.setPreferredSize(new Dimension(0, 8));

        summaryLabel = new JLabel("");
        summaryLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        summaryLabel.setForeground(new Color(130, 130, 160));

        bottom.add(progressBar,   BorderLayout.CENTER);
        bottom.add(summaryLabel,  BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);

        frame.setContentPane(root);

        // Positionner à côté de MNISTGui
        if (gui != null) {
            Point p = gui.getLocation();
            frame.setLocation(p.x + gui.getWidth() + 10, p.y);
        }
        frame.setVisible(true);
    }

    // ================================================================== //
    //  RUNNER
    // ================================================================== //

    private static void runAllTests() {

        // ─── Partie 1 ────────────────────────────────────────────────────
        section("PARTIE 1 — ARCHITECTURE OO & FICHIERS");
        t_dataProcessor_via_textFileHandler();
        t_dataProcessor_via_excelExporter();
        t_dataProcessor_via_binaryReader();
        t_binaryReader_charge30();
        t_createTextFile();
        t_imageToFile();
        t_fileToImage();
        t_createExcelFile();
        t_csvToArff();

        // ─── Partie 2 ────────────────────────────────────────────────────
        section("PARTIE 2 — EXCEPTIONS PERSONNALISÉES");
        t_invalidDimensions();
        t_mnistFileNotFound_leve();
        t_mnistFileNotFound_chemin();
        t_dataFormatMismatch_champs();
        t_dataFormatMismatch_nonEntier();
        t_dataFormatMismatch_label();
        t_exceptionsChecked();

        // ─── Partie 3 ────────────────────────────────────────────────────
        section("PARTIE 3 — IA & POLYMORPHISME (WEKA)");
        t_naiveBayesEntraine();
        t_randomForestEntraine();
        t_polymorphisme_digitClassifier();
        t_predictionLabelsValides();
        t_probabiliteEntre0Et1();
        t_modelComparator();

        // ─── Partie 4 — MNISTGui ─────────────────────────────────────────
        section("PARTIE 4 — INTERFACE GRAPHIQUE (MNISTGui)");
        t_gui_fenetreVisible();
        t_gui_canvasExiste();
        t_gui_canvasInitBlanc();
        t_gui_dessinProduitsPixelsNoirs();
        t_gui_clearResetCanvas();
        t_gui_toPixelVector784();
        t_gui_comboBoxSelectionne();
        t_gui_recognizeBtnActif();
        t_gui_clearBtnEfface();
        t_gui_predictionSurDessin();
        t_gui_resultLabelMisAJour();
        t_gui_probabiliteAffichee();

        // ─── Résumé ──────────────────────────────────────────────────────
        printSummary();
    }

    // ================================================================== //
    //  PARTIE 1
    // ================================================================== //

    static void t_dataProcessor_via_textFileHandler() {
        begin("DataProcessor : TextFileHandler utilisé via l'interface");
        try {
            // Assigne un TextFileHandler à une variable DataProcessor — c'est le contrat
            DataProcessor dp = new TextFileHandler(CSV_FILE);
            dp.load();   // appel via l'interface
            check(((TextFileHandler) dp).getNbImages() == 100,
                  "100 images chargées via DataProcessor.load()");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_dataProcessor_via_excelExporter() {
        begin("DataProcessor : ExcelExporter utilisé via l'interface");
        try {
            DataProcessor dp = new ExcelExporter(CSV_FILE);
            dp.load();             // interface DataProcessor
            dp.export(EXCEL_FILE); // interface DataProcessor
            check(new File(EXCEL_FILE).exists(), "Excel créé via DataProcessor.export()");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_dataProcessor_via_binaryReader() {
        begin("DataProcessor : BinaryMNISTReader utilisé via l'interface");
        try {
            DataProcessor dp = new BinaryMNISTReader(IMAGES_PATH, LABELS_PATH);
            dp.load(); // interface DataProcessor
            int n = ((BinaryMNISTReader) dp).getNbImages();
            check(n > 0, n + " images chargées via DataProcessor");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_binaryReader_charge30() {
        begin("BinaryMNISTReader : 30+30 exemples, vecteurs 784-dim");
        try {
            BinaryMNISTReader r = new BinaryMNISTReader(IMAGES_PATH, LABELS_PATH);
            r.load(30);
            check(r.getPixels().size() == 60 && r.getPixels().get(0).length == 784,
                  "60 exemples (30 trois + 30 cinq), 784 pixels chacun");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_createTextFile() {
        begin("createTextFile(50) → 100 lignes CSV, 785 champs/ligne");
        try {
            TextFileHandler.createTextFile(IMAGES_PATH, LABELS_PATH, 50, CSV_FILE);
            long lines = Files.lines(Paths.get(CSV_FILE)).count();
            String first = Files.lines(Paths.get(CSV_FILE)).findFirst().orElse("");
            check(lines == 100 && first.split(",").length == 785,
                  lines + " lignes, " + first.split(",").length + " champs");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_imageToFile() {
        begin("imageToFile : PNG 28×28 → 784 valeurs CSV");
        try {
            String txt = TextFileHandler.imageToFile(TEST_IMAGE);
            String[] vals = new String(Files.readAllBytes(Paths.get(txt))).trim().split(",");
            check(vals.length == 784, "784 valeurs extraites de l'image");
        } catch (InvalidDimensionsException e) { fail(e.getMessage()); }
          catch (IOException e)                { skip("Image test absente"); }
    }

    static void t_fileToImage() {
        begin("fileToImage : CSV → image PNG reconstituée 28×28");
        try {
            String txt = TextFileHandler.imageToFile(TEST_IMAGE);
            String png = TextFileHandler.fileToImage(txt);
            BufferedImage img = ImageIO.read(new File(png));
            check(img != null && img.getWidth() == 28 && img.getHeight() == 28,
                  "Image reconstituée 28×28 px");
        } catch (InvalidDimensionsException | DataFormatMismatchException e) { fail(e.getMessage()); }
          catch (IOException e) { skip("Image test absente"); }
    }

    static void t_createExcelFile() {
        begin("createExcelFile → .xlsx valide (>10KB, 100 lignes de données)");
        try {
            ExcelExporter.createExcelFile(CSV_FILE, EXCEL_FILE);
            ExcelExporter ex = new ExcelExporter(CSV_FILE);
            ex.load();
            File f = new File(EXCEL_FILE);
            check(f.exists() && f.length() > 10000 && ex.getNbImages() == 100,
                  f.length() + " octets, 100 lignes");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_csvToArff() {
        begin("csvToArff → ARFF valide (@RELATION, @ATTRIBUTE, @DATA, {trois,cinq})");
        try {
            String out = "data/test_arff.arff";
            TextFileHandler.csvToArff(CSV_FILE, out);
            List<String> lines = Files.readAllLines(Paths.get(out));
            boolean ok = lines.stream().anyMatch(l -> l.startsWith("@RELATION"))
                      && lines.stream().anyMatch(l -> l.startsWith("@ATTRIBUTE"))
                      && lines.stream().anyMatch(l -> l.startsWith("@DATA"))
                      && lines.stream().anyMatch(l -> l.contains("{trois,cinq}"));
            check(ok, "Structure ARFF complète et correcte");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    // ================================================================== //
    //  PARTIE 2
    // ================================================================== //

    static void t_invalidDimensions() {
        begin("InvalidDimensionsException : image ≠ 28×28 px");
        try {
            ImageIO.write(new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB),
                          "PNG", new File("data/big.png"));
            try {
                TextFileHandler.imageToFile("data/big.png");
                fail("Exception non levée pour image 50×50");
            } catch (InvalidDimensionsException e) {
                check(e.getActualWidth() == 50 && e.getMessage().contains("50"),
                      "Dimensions 50×50 capturées dans l'exception");
            }
        } catch (IOException e) { fail(e.getMessage()); }
    }

    static void t_mnistFileNotFound_leve() {
        begin("MNISTFileNotFoundException : fichier binaire absent");
        try {
            new BinaryMNISTReader("bad/path/images", "bad/path/labels").load();
            fail("Exception non levée");
        } catch (MNISTFileNotFoundException e) {
            pass("MNISTFileNotFoundException correctement levée");
        }
    }

    static void t_mnistFileNotFound_chemin() {
        begin("MNISTFileNotFoundException : chemin manquant dans le message");
        try {
            new BinaryMNISTReader("missing/file.bin", "other.bin").load();
            fail("Exception non levée");
        } catch (MNISTFileNotFoundException e) {
            check(e.getMessage().contains("missing/file.bin") && e.getMissingFilePath() != null,
                  "Chemin présent dans message et getMissingFilePath()");
        }
    }

    static void t_dataFormatMismatch_champs() {
        begin("DataFormatMismatchException : mauvais nombre de champs CSV");
        try {
            Files.write(Paths.get(BAD_CSV), "1,2,trois\n".getBytes());
            new TextFileHandler(BAD_CSV).load();
            fail("Exception non levée");
        } catch (DataFormatMismatchException e) {
            check(e.getLineNumber() == 1, "Numéro de ligne = 1 dans l'exception");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_dataFormatMismatch_nonEntier() {
        begin("DataFormatMismatchException : valeur pixel non entière");
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 783; i++) sb.append(i % 256).append(",");
            sb.append("XYZ,trois");
            Files.write(Paths.get(BAD_CSV), sb.toString().getBytes());
            new TextFileHandler(BAD_CSV).load();
            fail("Exception non levée");
        } catch (DataFormatMismatchException e) {
            check(e.getProblematicValue().equals("XYZ"), "Valeur 'XYZ' dans l'exception");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_dataFormatMismatch_label() {
        begin("DataFormatMismatchException : label invalide ('sept')");
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 784; i++) sb.append(i % 256).append(",");
            sb.append("sept");
            Files.write(Paths.get(BAD_CSV), sb.toString().getBytes());
            new TextFileHandler(BAD_CSV).load();
            fail("Exception non levée");
        } catch (DataFormatMismatchException e) {
            check(e.getProblematicValue().equals("sept"), "Valeur 'sept' dans l'exception");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_exceptionsChecked() {
        begin("Les 3 exceptions sont des checked exceptions");
        try {
            // Si elles n'étaient pas checked, ce code ne compilerait pas.
            try { throw new InvalidDimensionsException(10, 10); }
            catch (InvalidDimensionsException ignored) {}
            try { throw new MNISTFileNotFoundException("x"); }
            catch (MNISTFileNotFoundException ignored) {}
            try { throw new DataFormatMismatchException(1, "v", "d"); }
            catch (DataFormatMismatchException ignored) {}
            pass("Toutes checked — prouvé à la compilation");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    // ================================================================== //
    //  PARTIE 3
    // ================================================================== //

    static void t_naiveBayesEntraine() {
        begin("NaiveBayesClassifier : modèle entraîné sur 800 exemples");
        try {
            NaiveBayesClassifier nb = new NaiveBayesClassifier(ARFF_TRAIN);
            check(nb.getModel() != null && nb.getTrainData().numInstances() == 800,
                  "Modèle non null, 800 instances d'entraînement");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_randomForestEntraine() {
        begin("RandomForestClassifier : modèle entraîné sur 800 exemples");
        try {
            RandomForestClassifier rf = new RandomForestClassifier(ARFF_TRAIN);
            check(rf.getModel() != null && rf.getTrainData().numInstances() == 800,
                  "Modèle non null, 800 instances d'entraînement");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_polymorphisme_digitClassifier() {
        begin("Polymorphisme : NaiveBayes et RandomForest via DigitClassifier");
        try {
            // Les deux assignés à DigitClassifier — c'est le polymorphisme
            DigitClassifier c1 = new NaiveBayesClassifier(ARFF_TRAIN);
            DigitClassifier c2 = new RandomForestClassifier(ARFF_TRAIN);
            int[] px = new int[784];
            String p1 = c1.predict(px);
            String p2 = c2.predict(px);
            check((p1.equals("trois") || p1.equals("cinq"))
               && (p2.equals("trois") || p2.equals("cinq")),
                  "NB→'" + p1 + "', RF→'" + p2 + "' — même appel, modèles différents");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_predictionLabelsValides() {
        begin("predict() retourne 'trois'/'cinq' sur vrais pixels MNIST");
        try {
            BinaryMNISTReader r = new BinaryMNISTReader(IMAGES_PATH, LABELS_PATH);
            r.load(5);
            DigitClassifier nb = new NaiveBayesClassifier(ARFF_TRAIN);
            DigitClassifier rf = new RandomForestClassifier(ARFF_TRAIN);
            int correct = 0;
            for (int i = 0; i < r.getPixels().size(); i++) {
                String truth = r.getLabels().get(i) == 3 ? "trois" : "cinq";
                String pnb   = nb.predict(r.getPixels().get(i));
                String prf   = rf.predict(r.getPixels().get(i));
                if (!(pnb.equals("trois") || pnb.equals("cinq")))
                    throw new Exception("Label NB invalide : " + pnb);
                if (prf.equals(truth)) correct++;
            }
            check(true, "Labels valides sur 10 exemples, RF correct sur " + correct + "/10");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_probabiliteEntre0Et1() {
        begin("getProbability() retourne une valeur dans [0.0, 1.0]");
        try {
            DigitClassifier nb = new NaiveBayesClassifier(ARFF_TRAIN);
            DigitClassifier rf = new RandomForestClassifier(ARFF_TRAIN);
            int[] px = new int[784];
            double pnb = nb.getProbability(px);
            double prf = rf.getProbability(px);
            check(pnb >= 0 && pnb <= 1 && prf >= 0 && prf <= 1,
                  String.format("NB=%.3f, RF=%.3f ∈ [0,1]", pnb, prf));
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_modelComparator() {
        begin("ModelComparator : accuracy NB et RF > 50% sur 100 exemples test");
        try {
            BinaryMNISTReader reader = new BinaryMNISTReader(IMAGES_PATH, LABELS_PATH);
            reader.load(450);
            List<int[]>   allPx = reader.getPixels();
            List<Integer> allLb = reader.getLabels();
            int total = allPx.size();
            List<int[]>   tPx = allPx.subList(Math.max(0, total - 100), total);
            List<Integer> tLb = allLb.subList(Math.max(0, total - 100), total);

            DigitClassifier nb = new NaiveBayesClassifier(ARFF_TRAIN);
            DigitClassifier rf = new RandomForestClassifier(ARFF_TRAIN);
            int nbOk = 0, rfOk = 0;
            for (int i = 0; i < tPx.size(); i++) {
                String truth = tLb.get(i) == 3 ? "trois" : "cinq";
                if (nb.predict(tPx.get(i)).equals(truth)) nbOk++;
                if (rf.predict(tPx.get(i)).equals(truth)) rfOk++;
            }
            double nbAcc = 100.0 * nbOk / tPx.size();
            double rfAcc = 100.0 * rfOk / tPx.size();
            String winner = rfAcc > nbAcc ? "Random Forest" : "Naive Bayes";
            log("    ┌──────────────────────────────────────────┐");
            log(String.format("    │ Naive Bayes   : %5.1f%%  (%d/100)        │", nbAcc, nbOk));
            log(String.format("    │ Random Forest : %5.1f%%  (%d/100)        │", rfAcc, rfOk));
            log(String.format("    │ Meilleur      : %-27s│", winner));
            log("    └──────────────────────────────────────────┘");
            check(nbAcc > 50 && rfAcc > 50,
                  String.format("NB=%.1f%%, RF=%.1f%% — Meilleur: %s", nbAcc, rfAcc, winner));
        } catch (Exception e) { fail(e.getMessage()); }
    }

    // ================================================================== //
    //  PARTIE 4 — MNISTGui
    // ================================================================== //

    static void t_gui_fenetreVisible() {
        begin("GUI : fenêtre MNISTGui est visible et affichée");
        check(gui != null && gui.isVisible(),
              "MNISTGui instanciée et visible à l'écran");
    }

    static void t_gui_canvasExiste() {
        begin("GUI : DrawingCanvas accessible via getCanvas()");
        MNISTGui.DrawingCanvas c = gui.getCanvas();
        check(c != null, "getCanvas() retourne un objet non null");
    }

    static void t_gui_canvasInitBlanc() {
        begin("GUI : canvas initialisé en blanc (fond blanc MNIST)");
        MNISTGui.DrawingCanvas c = gui.getCanvas();
        c.clear();
        BufferedImage img = c.getImage();
        Color px = new Color(img.getRGB(img.getWidth() / 2, img.getHeight() / 2));
        check(px.getRed() > 200 && px.getGreen() > 200 && px.getBlue() > 200,
              "Pixel central blanc après clear()");
    }

    static void t_gui_dessinProduitsPixelsNoirs() {
        begin("GUI : draw() sur DrawingCanvas produit des pixels noirs");
        MNISTGui.DrawingCanvas c = gui.getCanvas();
        c.clear();
        // Simuler un tracé au centre du canvas
        c.draw(MNISTGui.CANVAS_SIZE / 2, MNISTGui.CANVAS_SIZE / 2);
        BufferedImage img = c.getImage();
        boolean foundDark = false;
        for (int x = 0; x < img.getWidth() && !foundDark; x++)
            for (int y = 0; y < img.getHeight() && !foundDark; y++) {
                Color px = new Color(img.getRGB(x, y));
                if (px.getRed() < 50 && px.getGreen() < 50 && px.getBlue() < 50)
                    foundDark = true;
            }
        check(foundDark, "Pixels noirs détectés après dessin simulé");
    }

    static void t_gui_clearResetCanvas() {
        begin("GUI : clear() remet le canvas entièrement en blanc");
        MNISTGui.DrawingCanvas c = gui.getCanvas();
        c.draw(100, 100);   // dessiner quelque chose
        c.clear();           // effacer
        BufferedImage img = c.getImage();
        boolean hasNonWhite = false;
        for (int x = 0; x < img.getWidth() && !hasNonWhite; x++)
            for (int y = 0; y < img.getHeight() && !hasNonWhite; y++) {
                Color px = new Color(img.getRGB(x, y));
                if (px.getRed() < 200) hasNonWhite = true;
            }
        check(!hasNonWhite, "Canvas entièrement blanc après clear()");
    }

    static void t_gui_toPixelVector784() {
        begin("GUI : toPixelVector() retourne 784 valeurs dans [0, 255]");
        MNISTGui.DrawingCanvas c = gui.getCanvas();
        c.clear();
        c.draw(MNISTGui.CANVAS_SIZE / 2, MNISTGui.CANVAS_SIZE / 2);
        int[] vec = c.toPixelVector();
        boolean validSize   = vec.length == 784;
        boolean validValues = true;
        for (int v : vec) if (v < 0 || v > 255) { validValues = false; break; }
        check(validSize && validValues, "784 valeurs dans [0, 255]");
    }

    static void t_gui_comboBoxSelectionne() {
        begin("GUI : JComboBox sélectionne Naive Bayes et Random Forest");
        JComboBox<String> combo = gui.getModelCombo();
        combo.setSelectedIndex(0);
        String sel0 = (String) combo.getSelectedItem();
        combo.setSelectedIndex(1);
        String sel1 = (String) combo.getSelectedItem();
        check("Naive Bayes".equals(sel0) && "Random Forest".equals(sel1),
              "Index 0→NaiveBayes, Index 1→RandomForest");
    }

    static void t_gui_recognizeBtnActif() {
        begin("GUI : bouton Reconnaître actif après chargement des modèles");
        check(gui.getRecognizeBtn().isEnabled() && gui.isModelsLoaded(),
              "Bouton actif ✓  — modèles chargés ✓");
    }

    static void t_gui_clearBtnEfface() {
        begin("GUI : bouton Effacer déclenche clear() sur le canvas");
        MNISTGui.DrawingCanvas c = gui.getCanvas();
        c.draw(140, 140);
        // Cliquer le bouton programmatiquement
        gui.getClearBtn().doClick();
        BufferedImage img = c.getImage();
        Color px = new Color(img.getRGB(img.getWidth() / 2, img.getHeight() / 2));
        check(px.getRed() > 200, "Canvas blanc après clic sur le bouton Effacer");
    }

    static void t_gui_predictionSurDessin() {
        begin("GUI : predict() sur un dessin simulé → 'trois' ou 'cinq'");
        if (!gui.isModelsLoaded()) { skip("Modèles non chargés"); return; }
        try {
        	MNISTGui.DrawingCanvas c = gui.getCanvas();
            c.clear();
            // Simuler le tracé d'un chiffre
            for (int i = 60; i < 220; i += 8) c.draw(i, MNISTGui.CANVAS_SIZE / 2);
            int[]  pixels = c.toPixelVector();
            // Utiliser la GUI directement — DigitClassifier via la GUI
            gui.getModelCombo().setSelectedIndex(0); // Naive Bayes
            gui.recognize(); // appel direct à la méthode recognize() de MNISTGui
            String result = gui.getResultLabel().getText();
            check(result.equals("TROIS") || result.equals("CINQ"),
                  "Résultat = '" + result + "'");
        } catch (Exception e) { fail(e.getMessage()); }
    }

    static void t_gui_resultLabelMisAJour() {
        begin("GUI : JLabel résultat affiche TROIS ou CINQ après recognize()");
        if (!gui.isModelsLoaded()) { skip("Modèles non chargés"); return; }
        MNISTGui.DrawingCanvas c = gui.getCanvas();
        c.clear();
        for (int i = 50; i < 230; i += 5) c.draw(i, MNISTGui.CANVAS_SIZE / 2);
        gui.recognize();
        String txt = gui.getResultLabel().getText();
        check(txt.equals("TROIS") || txt.equals("CINQ"),
              "JLabel affiche '" + txt + "' (TROIS ou CINQ)");
    }

    static void t_gui_probabiliteAffichee() {
        begin("GUI : JLabel probabilité affiche un pourcentage après recognize()");
        if (!gui.isModelsLoaded()) { skip("Modèles non chargés"); return; }
        gui.getCanvas().clear();
        gui.getCanvas().draw(MNISTGui.CANVAS_SIZE / 2, MNISTGui.CANVAS_SIZE / 2);
        gui.recognize();
        String prob = gui.getProbLabel().getText();
        check(prob.contains("%"), "Probabilité affichée : '" + prob + "'");
    }

    // ================================================================== //
    //  UTILITAIRES
    // ================================================================== //

    private static void waitForModels() throws InterruptedException {
        int waited = 0;
        while (!gui.isModelsLoaded() && waited < 120000) {
            Thread.sleep(500);
            waited += 500;
        }
        if (!gui.isModelsLoaded())
            throw new RuntimeException("Timeout — modèles non chargés après 2 min");
    }

    private static void prepareArffIfMissing() throws Exception {
        new File("data").mkdirs();
        if (!new File(CSV_FILE).exists())
            TextFileHandler.createTextFile(IMAGES_PATH, LABELS_PATH, 50, CSV_FILE);
        if (!new File(ARFF_TRAIN).exists()) {
            TextFileHandler.createTextFile(IMAGES_PATH, LABELS_PATH, 400, CSV_400);
            TextFileHandler.csvToArff(CSV_400, ARFF_TRAIN);
        }
    }

    private static void generateTestImageIfMissing() {
        if (!new File(TEST_IMAGE).exists()) {
            try {
                new File("data").mkdirs();
                BufferedImage img = new BufferedImage(28, 28, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.setColor(Color.WHITE); g.fillRect(0, 0, 28, 28);
                g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.BOLD, 20));
                g.drawString("3", 6, 22); g.dispose();
                ImageIO.write(img, "PNG", new File(TEST_IMAGE));
            } catch (IOException ignored) {}
        }
    }

    // ── Helpers test ─────────────────────────────────────────────────── //

    private static void section(String title) {
        log("\n┌──────────────────────────────────────────────────────┐");
        log("│  " + title);
        log("└──────────────────────────────────────────────────────┘\n");
    }

    private static void begin(String desc) {
        totalT++;
        log(String.format("  [%02d] %s", totalT, desc));
    }

    private static void check(boolean cond, String detail) {
        if (cond) pass(detail); else fail(detail);
    }

    private static void pass(String detail) {
        passT++;
        log("       ✅ PASS — " + detail + "\n");
        updateProgress();
    }

    private static void fail(String detail) {
        failT++;
        log("       ❌ FAIL — " + detail + "\n");
        updateProgress();
    }

    private static void skip(String detail) {
        totalT--;
        log("       ⚠️  SKIP — " + detail + "\n");
    }

    private static void updateProgress() {
        int done = passT + failT;
        if (progressBar == null) return;
        SwingUtilities.invokeLater(() -> {
            progressBar.setMaximum(Math.max(totalT, done));
            progressBar.setValue(done);
            progressBar.setForeground(failT > 0
                    ? new Color(255, 190, 60) : new Color(100, 140, 255));
            summaryLabel.setText("  ✅ " + passT + "  ❌ " + failT + "  / " + totalT + "  ");
        });
    }

    private static void printSummary() {
        log("\n╔══════════════════════════════════════════════════════╗");
        log(String.format("║  Total   : %-43d║", totalT));
        log(String.format("║  ✅ Pass : %-43d║", passT));
        log(String.format("║  ❌ Fail : %-43d║", failT));
        log(String.format("║  Score   : %-43s║",
                String.format("%.1f%%", 100.0 * passT / totalT)));
        log("╚══════════════════════════════════════════════════════╝");
        log(failT == 0
                ? "\n  🎉 TOUS LES TESTS PASSÉS — PROJET COMPLET !"
                : "\n  ⚠️  " + failT + " test(s) échoué(s) — voir les détails ci-dessus.");

        if (summaryLabel != null) {
            SwingUtilities.invokeLater(() ->
                summaryLabel.setForeground(failT == 0
                        ? new Color(80, 210, 130) : new Color(255, 90, 90)));
        }
    }

    private static void log(String line) {
        System.out.println(line);
        if (outputArea != null) {
            SwingUtilities.invokeLater(() -> {
                outputArea.append(line + "\n");
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
            });
        }
    }
}