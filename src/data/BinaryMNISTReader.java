package data;

import exceptions.MNISTFileNotFoundException;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Lit les fichiers binaires MNIST au format IDX.
 *
 * Format IDX3 (images) :
 *   - magic number  : 4 octets (0x00000803)
 *   - nb images     : 4 octets (int big-endian)
 *   - nb rows       : 4 octets
 *   - nb cols       : 4 octets
 *   - pixels        : nb_images × 784 octets
 *
 * Format IDX1 (labels) :
 *   - magic number  : 4 octets (0x00000801)
 *   - nb items      : 4 octets
 *   - labels        : nb_items octets
 */
public class BinaryMNISTReader extends MNISTProvider {

    private static final int MAGIC_IMAGES = 2051;
    private static final int MAGIC_LABELS = 2049;

    private final String imagesPath;
    private final String labelsPath;

    /** Stocke les vecteurs de pixels et les labels après chargement. */
    private List<int[]>  pixels;
    private List<Integer> labels;

    // ------------------------------------------------------------------ //
    //  Constructeur
    // ------------------------------------------------------------------ //

    public BinaryMNISTReader(String imagesPath, String labelsPath) {
        super();
        this.imagesPath = imagesPath;
        this.labelsPath = labelsPath;
        this.pixels     = new ArrayList<>();
        this.labels     = new ArrayList<>();
    }

    // ------------------------------------------------------------------ //
    //  DataProcessor
    // ------------------------------------------------------------------ //

    /**
     * Charge TOUS les exemples (3 et 5 uniquement) depuis les fichiers binaires.
     */
    @Override
    public void load() throws MNISTFileNotFoundException {
        loadFiltered(-1); // charge tout
    }

    /**
     * Charge au plus {@code maxPerClass} exemples de chaque classe (3 et 5).
     *
     * @param maxPerClass nombre maximum d'exemples par classe (-1 = tout)
     */
    public void load(int maxPerClass) throws MNISTFileNotFoundException {
        loadFiltered(maxPerClass);
    }

    @Override
    public void export(String destination) throws Exception {
        // L'export texte est géré par TextFileHandler.
        // Ici on délègue en créant un TextFileHandler à partir des données chargées.
        if (pixels.isEmpty()) {
            System.out.println("Aucune donnée chargée. Appelez load() d'abord.");
            return;
        }
        TextFileHandler tfh = new TextFileHandler(pixels, labels);
        tfh.export(destination);
    }

    // ------------------------------------------------------------------ //
    //  Lecture interne
    // ------------------------------------------------------------------ //

    private void loadFiltered(int maxPerClass) throws MNISTFileNotFoundException {
        pixels.clear();
        labels.clear();

        // Vérifie l'existence des fichiers
        File imgFile = new File(imagesPath);
        File lblFile = new File(labelsPath);

        if (!imgFile.exists() || !imgFile.canRead()) {
            throw new MNISTFileNotFoundException(imagesPath);
        }
        if (!lblFile.exists() || !lblFile.canRead()) {
            throw new MNISTFileNotFoundException(labelsPath);
        }

        try (DataInputStream imgStream = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(imgFile)));
             DataInputStream lblStream = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(lblFile)))) {

            // --- Lecture de l'en-tête des images ---
            int imgMagic = imgStream.readInt();
            if (imgMagic != MAGIC_IMAGES) {
                throw new MNISTFileNotFoundException(imagesPath +
                        " (magic number invalide : " + imgMagic + ")");
            }
            int totalImages = imgStream.readInt();
            int rows        = imgStream.readInt();
            int cols        = imgStream.readInt();
            int pixelCount  = rows * cols;

            // --- Lecture de l'en-tête des labels ---
            int lblMagic = lblStream.readInt();
            if (lblMagic != MAGIC_LABELS) {
                throw new MNISTFileNotFoundException(labelsPath +
                        " (magic number invalide : " + lblMagic + ")");
            }
            int totalLabels = lblStream.readInt();

            if (totalImages != totalLabels) {
                throw new MNISTFileNotFoundException(
                        "Incohérence : " + totalImages + " images mais " + totalLabels + " labels.");
            }

            int countTrois = 0, countCinq = 0;

            for (int i = 0; i < totalImages; i++) {
                byte[] rawPixels = new byte[pixelCount];
                imgStream.readFully(rawPixels);
                int label = lblStream.readUnsignedByte();

                if (label == 3) {
                    if (maxPerClass < 0 || countTrois < maxPerClass) {
                        pixels.add(toUnsigned(rawPixels));
                        labels.add(label);
                        countTrois++;
                    }
                } else if (label == 5) {
                    if (maxPerClass < 0 || countCinq < maxPerClass) {
                        pixels.add(toUnsigned(rawPixels));
                        labels.add(label);
                        countCinq++;
                    }
                }

                // Arrêt anticipé si on a assez d'exemples des deux classes
                if (maxPerClass > 0 && countTrois >= maxPerClass && countCinq >= maxPerClass) {
                    break;
                }
            }

            this.nbImages = pixels.size();
            System.out.printf("Chargement terminé : %d exemples de « trois », %d de « cinq »%n",
                    countTrois, countCinq);

        } catch (MNISTFileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new MNISTFileNotFoundException(imagesPath + " ou " + labelsPath, e);
        }
    }

    private int[] toUnsigned(byte[] raw) {
        int[] result = new int[raw.length];
        for (int i = 0; i < raw.length; i++) {
            result[i] = raw[i] & 0xFF;
        }
        return result;
    }

    // ------------------------------------------------------------------ //
    //  Accesseurs
    // ------------------------------------------------------------------ //

    public List<int[]>   getPixels() { return pixels; }
    public List<Integer> getLabels() { return labels; }
}
