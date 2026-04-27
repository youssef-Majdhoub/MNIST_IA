package data;

import exceptions.DataFormatMismatchException;
import exceptions.InvalidDimensionsException;
import exceptions.MNISTFileNotFoundException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère la création, la lecture et la manipulation des fichiers texte CSV
 * issus des données MNIST, ainsi que la conversion image ↔ fichier texte.
 */
public class TextFileHandler extends MNISTProvider {

    private String filePath;
    private List<int[]>   pixels;
    private List<Integer> labels;

    // ------------------------------------------------------------------ //
    //  Constructeurs
    // ------------------------------------------------------------------ //

    /** Constructeur pour créer un handler sur un fichier existant. */
    public TextFileHandler(String filePath) {
        super();
        this.filePath = filePath;
        this.pixels   = new ArrayList<>();
        this.labels   = new ArrayList<>();
    }

    /** Constructeur interne utilisé par BinaryMNISTReader pour l'export. */
    public TextFileHandler(List<int[]> pixels, List<Integer> labels) {
        super(pixels.size());
        this.filePath = null;
        this.pixels   = new ArrayList<>(pixels);
        this.labels   = new ArrayList<>(labels);
    }

    // ------------------------------------------------------------------ //
    //  DataProcessor
    // ------------------------------------------------------------------ //

    /**
     * Charge les données depuis le fichier CSV (chiffres.txt).
     */
    @Override
    public void load() throws DataFormatMismatchException, MNISTFileNotFoundException {
        if (filePath == null) {
            throw new IllegalStateException("Aucun chemin de fichier défini.");
        }
        File f = new File(filePath);
        if (!f.exists() || !f.canRead()) {
            throw new MNISTFileNotFoundException(filePath);
        }

        pixels.clear();
        labels.clear();
        int lineNumber = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;

                String[] parts = line.split(",");
                // 784 pixels + 1 label = 785 champs
                if (parts.length != VECTOR_SIZE + 1) {
                    throw new DataFormatMismatchException(
                            lineNumber,
                            String.valueOf(parts.length),
                            "Attendu 785 champs (784 pixels + 1 label), trouvé " + parts.length + "."
                    );
                }

                int[] pixelRow = new int[VECTOR_SIZE];
                for (int i = 0; i < VECTOR_SIZE; i++) {
                    try {
                        pixelRow[i] = Integer.parseInt(parts[i].trim());
                    } catch (NumberFormatException e) {
                        throw new DataFormatMismatchException(
                                lineNumber,
                                parts[i].trim(),
                                "La valeur n'est pas un entier valide.",
                                e
                        );
                    }
                }

                String labelStr = parts[VECTOR_SIZE].trim();
                int numericLabel;
                if (LABEL_TROIS.equals(labelStr)) {
                    numericLabel = 3;
                } else if (LABEL_CINQ.equals(labelStr)) {
                    numericLabel = 5;
                } else {
                    throw new DataFormatMismatchException(
                            lineNumber,
                            labelStr,
                            "Le label doit être 'trois' ou 'cinq'."
                    );
                }

                pixels.add(pixelRow);
                labels.add(numericLabel);
            }
        } catch (DataFormatMismatchException | MNISTFileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new MNISTFileNotFoundException(filePath, e);
        }

        this.nbImages = pixels.size();
        System.out.println("Fichier chargé : " + nbImages + " lignes lues depuis " + filePath);
    }

    /**
     * Exporte les données en mémoire vers un fichier CSV.
     */
    @Override
    public void export(String destination) throws IOException {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(destination)))) {
            for (int i = 0; i < pixels.size(); i++) {
                StringBuilder sb = new StringBuilder();
                int[] row = pixels.get(i);
                for (int j = 0; j < row.length; j++) {
                    sb.append(row[j]);
                    sb.append(',');
                }
                sb.append(toFrenchLabel(labels.get(i)));
                pw.println(sb.toString());
            }
        }
        System.out.println("Export CSV terminé : " + destination
                + " (" + pixels.size() + " lignes)");
    }

    // ------------------------------------------------------------------ //
    //  Méthodes métier (Partie 1)
    // ------------------------------------------------------------------ //

    /**
     * Génère le fichier {@code chiffres.txt} à partir des fichiers binaires MNIST.
     * Lit les n premiers exemples de « trois » et les n premiers « cinq ».
     *
     * @param imagesPath chemin vers train-images
     * @param labelsPath chemin vers train-labels
     * @param n          nombre d'exemples par classe
     * @param outputPath chemin du fichier texte à créer
     */
    public static void createTextFile(String imagesPath, String labelsPath,
                                      int n, String outputPath)
            throws Exception {

        BinaryMNISTReader reader = new BinaryMNISTReader(imagesPath, labelsPath);
        reader.load(n);
        reader.export(outputPath);
        System.out.println("createTextFile(" + n + ") → " + outputPath
                + " (" + (reader.getPixels().size()) + " lignes)");
    }

    /**
     * Accepte une image PNG 28×28 px et écrit dans un fichier texte
     * (même nom, extension .txt) une ligne CSV des 784 valeurs d'intensité.
     *
     * @param nomImage chemin vers l'image PNG
     * @return le chemin du fichier texte généré
     */
    public static String imageToFile(String nomImage)
            throws InvalidDimensionsException, IOException {

        BufferedImage img = ImageIO.read(new File(nomImage));
        if (img == null) {
            throw new IOException("Impossible de lire l'image : " + nomImage);
        }

        int w = img.getWidth();
        int h = img.getHeight();
        if (w != IMAGE_SIZE || h != IMAGE_SIZE) {
            throw new InvalidDimensionsException(w, h);
        }

        // Construction du nom du fichier de sortie
        String outputPath = nomImage.replaceAll("\\.[^.]+$", "") + ".txt";

        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < IMAGE_SIZE; y++) {
            for (int x = 0; x < IMAGE_SIZE; x++) {
                Color c   = new Color(img.getRGB(x, y));
                int   grey = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                sb.append(grey);
                if (!(y == IMAGE_SIZE - 1 && x == IMAGE_SIZE - 1)) {
                    sb.append(',');
                }
            }
        }

        Files.write(Paths.get(outputPath), sb.toString().getBytes());
        System.out.println("imageToFile → " + outputPath);
        return outputPath;
    }

    /**
     * Recrée une image PNG 28×28 px à partir d'un fichier texte CSV de 784 valeurs.
     *
     * @param nomFichier chemin vers le fichier texte CSV
     * @return le chemin de l'image PNG générée
     */
    public static String fileToImage(String nomFichier)
            throws DataFormatMismatchException, IOException {

        String content = new String(Files.readAllBytes(Paths.get(nomFichier))).trim();
        String[] parts = content.split(",");

        // On autorise 784 (sans label) ou 785 (avec label)
        if (parts.length < VECTOR_SIZE) {
            throw new DataFormatMismatchException(
                    1, String.valueOf(parts.length),
                    "Attendu au moins 784 valeurs, trouvé " + parts.length + "."
            );
        }

        BufferedImage img = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE,
                BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < VECTOR_SIZE; i++) {
            int val;
            try {
                val = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                throw new DataFormatMismatchException(1, parts[i].trim(),
                        "Valeur non entière à la position " + i + ".", e);
            }
            val = Math.max(0, Math.min(255, val));
            int rgb = new Color(val, val, val).getRGB();
            img.setRGB(i % IMAGE_SIZE, i / IMAGE_SIZE, rgb);
        }

        String outputPath = nomFichier.replaceAll("\\.[^.]+$", "") + "_reconstitue.png";
        ImageIO.write(img, "PNG", new File(outputPath));
        System.out.println("fileToImage → " + outputPath);
        return outputPath;
    }

    /**
     * Convertit un fichier CSV (format createTextFile) en fichier ARFF pour Weka.
     *
     * @param src chemin du fichier CSV source
     * @param dst chemin du fichier ARFF à créer
     */
    public static void csvToArff(String src, String dst) throws IOException {

        File srcFile = new File(src);
        long lineCount = Files.lines(Paths.get(src)).count();

        try (BufferedReader br = new BufferedReader(new FileReader(srcFile));
             PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(dst)))) {

            // En-tête ARFF
            pw.println("@RELATION mnist_chiffres");
            pw.println();
            for (int i = 0; i < VECTOR_SIZE; i++) {
                pw.println("@ATTRIBUTE pixel" + i + " NUMERIC");
            }
            pw.println("@ATTRIBUTE label {trois,cinq}");
            pw.println();
            pw.println("@DATA");

            // Copie directe des lignes CSV
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    pw.println(line);
                }
            }
        }
        System.out.println("csvToArff → " + dst + " (" + lineCount + " exemples)");
    }

    // ------------------------------------------------------------------ //
    //  Accesseurs
    // ------------------------------------------------------------------ //

    public List<int[]>   getPixels() { return pixels; }
    public List<Integer> getLabels() { return labels; }
    public String        getFilePath() { return filePath; }
}
