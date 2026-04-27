package test;

import data.BinaryMNISTReader;
import data.ExcelExporter;
import data.TextFileHandler;
import exceptions.DataFormatMismatchException;
import exceptions.InvalidDimensionsException;
import exceptions.MNISTFileNotFoundException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Classe de test pour la Partie 1 — Architecture OO et Traitement de Fichiers.
 *
 * Pré-requis :
 *   Les fichiers MNIST binaires doivent être dans le dossier « data/ » :
 *     - data/train-images-idx3-ubyte
 *     - data/train-labels-idx1-ubyte
 *   Une image PNG 28×28 de test doit être fournie pour imageToFile / fileToImage.
 */
public class TestPartie1 {

    // Adaptez ces chemins selon votre installation
    private static final String IMAGES_PATH = "data/train-images.idx3-ubyte";
    private static final String LABELS_PATH = "data/train-labels.idx1-ubyte";
    private static final String TEXT_FILE   = "data/chiffres.txt";
    private static final String EXCEL_FILE  = "data/chiffres.xlsx";
    private static final String TEST_IMAGE  = "data/test_image.png";

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║            TEST PARTIE 1 — MNIST OO              ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        // ----------------------------------------------------------------
        // Test 1 : createTextFile(50)
        // ----------------------------------------------------------------
        System.out.println("─── Test 1 : createTextFile(50) ───────────────────");
        try {
            TextFileHandler.createTextFile(IMAGES_PATH, LABELS_PATH, 50, TEXT_FILE);

            long lineCount = Files.lines(Paths.get(TEXT_FILE)).count();
            System.out.println("✓ Fichier généré : " + TEXT_FILE);
            System.out.println("  Nombre de lignes : " + lineCount
                    + " (attendu : 100 = 50 trois + 50 cinq)");

        } catch (MNISTFileNotFoundException e) {
            System.err.println("✗ Fichier MNIST introuvable : " + e.getMessage());
            System.err.println("  → Vérifiez les chemins : " + IMAGES_PATH + " / " + LABELS_PATH);
        } catch (Exception e) {
            System.err.println("✗ Erreur inattendue : " + e.getMessage());
        }

        System.out.println();

        // ----------------------------------------------------------------
        // Test 2 : imageToFile + fileToImage
        // ----------------------------------------------------------------
        System.out.println("─── Test 2 : imageToFile + fileToImage ─────────────");
        try {
            // imageToFile
            String txtPath = TextFileHandler.imageToFile(TEST_IMAGE);
            System.out.println("✓ imageToFile → " + txtPath);

            // Vérifier le contenu
            String content = new String(Files.readAllBytes(Paths.get(txtPath)));
            long nbValeurs = content.chars().filter(c -> c == ',').count() + 1;
            System.out.println("  Nombre de valeurs dans le fichier : " + nbValeurs
                    + " (attendu : 784)");

            // fileToImage
            String pngPath = TextFileHandler.fileToImage(txtPath);
            System.out.println("✓ fileToImage → " + pngPath);

        } catch (InvalidDimensionsException e) {
            System.err.println("✗ Dimensions invalides : " + e.getMessage());
        } catch (DataFormatMismatchException e) {
            System.err.println("✗ Format de données invalide : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("✗ Image de test introuvable : " + TEST_IMAGE);
            System.err.println("  → Créez ou fournissez une image PNG 28×28 px.");
        }

        System.out.println();

        // ----------------------------------------------------------------
        // Test 3 : createExcelFile
        // ----------------------------------------------------------------
        System.out.println("─── Test 3 : createExcelFile ───────────────────────");
        try {
            // Vérifier que le fichier texte existe
            if (!new File(TEXT_FILE).exists()) {
                System.out.println("  Le fichier " + TEXT_FILE + " n'existe pas encore.");
                System.out.println("  → Relancez le Test 1 d'abord.");
            } else {
                ExcelExporter.createExcelFile(TEXT_FILE, EXCEL_FILE);

                // Vérification basique du fichier Excel créé
                File xlsxFile = new File(EXCEL_FILE);
                System.out.println("✓ Fichier Excel créé : " + EXCEL_FILE);
                System.out.println("  Taille du fichier : " + xlsxFile.length() + " octets");

                // Lecture du nombre de lignes via ExcelExporter
                ExcelExporter exporter = new ExcelExporter(TEXT_FILE);
                exporter.load();
                System.out.println("  Lignes de données : " + exporter.getNbImages()
                        + " (+ 1 ligne d'en-tête)");
            }
        } catch (MNISTFileNotFoundException e) {
            System.err.println("✗ Fichier source introuvable : " + e.getMessage());
        } catch (DataFormatMismatchException e) {
            System.err.println("✗ Format invalide dans le fichier source : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de la création Excel : " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();

        // ----------------------------------------------------------------
        // Test 4 : Démonstration des exceptions
        // ----------------------------------------------------------------
        System.out.println("─── Test 4 : Démonstration des exceptions ──────────");

        // InvalidDimensionsException
        try {
            TextFileHandler.imageToFile("data/fake_image_50x50.png");
        } catch (InvalidDimensionsException e) {
            System.out.println("✓ InvalidDimensionsException correctement levée.");
            System.out.println("  Message : " + e.getMessage());
        } catch (IOException e) {
            System.out.println("  (L'image de test n'existe pas — exception non testée)");
        }

        // MNISTFileNotFoundException
        try {
            BinaryMNISTReader bad = new BinaryMNISTReader("bad/path/images", "bad/path/labels");
            bad.load();
        } catch (MNISTFileNotFoundException e) {
            System.out.println("✓ MNISTFileNotFoundException correctement levée.");
            System.out.println("  Message : " + e.getMessage());
        }

        // DataFormatMismatchException
        try {
            // Créer un fichier CSV mal formé
            String badCsv = "data/bad_format.txt";
            Files.write(Paths.get(badCsv), "1,2,trois\n".getBytes());
            TextFileHandler handler = new TextFileHandler(badCsv);
            handler.load();
        } catch (DataFormatMismatchException e) {
            System.out.println("✓ DataFormatMismatchException correctement levée.");
            System.out.println("  Message : " + e.getMessage());
        } catch (MNISTFileNotFoundException e) {
            System.err.println("✗ " + e.getMessage());
        } catch (IOException e) {
            System.err.println("✗ Erreur E/S : " + e.getMessage());
        }

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║         FIN DES TESTS — PARTIE 1                 ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
}
