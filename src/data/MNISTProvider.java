package data;

import interfaces.DataProcessor;

/**
 * Classe abstraite fournissant les attributs et comportements communs
 * à tous les traitements de données MNIST.
 */
public abstract class MNISTProvider implements DataProcessor {

    /** Nombre d'images actuellement chargées. */
    protected int nbImages;

    /** Résolution de chaque image (toujours 28×28 pour MNIST). */
    protected static final int IMAGE_SIZE   = 28;
    protected static final int VECTOR_SIZE  = IMAGE_SIZE * IMAGE_SIZE; // 784

    /** Labels utilisés dans ce projet. */
    protected static final String LABEL_TROIS = "trois";
    protected static final String LABEL_CINQ  = "cinq";

    // ------------------------------------------------------------------ //
    //  Constructeurs
    // ------------------------------------------------------------------ //

    protected MNISTProvider() {
        this.nbImages = 0;
    }

    protected MNISTProvider(int nbImages) {
        this.nbImages = nbImages;
    }

    // ------------------------------------------------------------------ //
    //  Méthodes concrètes communes
    // ------------------------------------------------------------------ //

    /**
     * Retourne le nombre d'images actuellement gérées.
     */
    public int getNbImages() {
        return nbImages;
    }

    /**
     * Retourne la taille du vecteur de pixels (784).
     */
    public int getVectorSize() {
        return VECTOR_SIZE;
    }

    /**
     * Retourne la résolution (côté du carré : 28).
     */
    public int getImageSize() {
        return IMAGE_SIZE;
    }

    /**
     * Indique si l'étiquette fournie correspond à un chiffre pris en charge.
     */
    protected boolean isValidLabel(int label) {
        return label == 3 || label == 5;
    }

    /**
     * Convertit un label numérique MNIST (3 ou 5) en chaîne française.
     */
    protected String toFrenchLabel(int label) {
        return (label == 3) ? LABEL_TROIS : LABEL_CINQ;
    }

    /**
     * Convertit une chaîne française en label numérique MNIST.
     */
    protected int toNumericLabel(String frenchLabel) {
        return LABEL_TROIS.equals(frenchLabel) ? 3 : 5;
    }

    // ------------------------------------------------------------------ //
    //  Méthodes abstraites que les sous-classes doivent implémenter
    // ------------------------------------------------------------------ //

    @Override
    public abstract void load() throws Exception;

    @Override
    public abstract void export(String destination) throws Exception;
}
