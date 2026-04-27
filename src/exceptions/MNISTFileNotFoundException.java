package exceptions;

/**
 * Exception levée lorsque les fichiers binaires MNIST sont absents ou illisibles.
 */
public class MNISTFileNotFoundException extends Exception {

    private final String missingFilePath;

    /**
     * @param filePath chemin du fichier manquant ou illisible
     */
    public MNISTFileNotFoundException(String filePath) {
        super(String.format(
            "Fichier MNIST introuvable ou illisible : '%s'. " +
            "Vérifiez que les fichiers train-images et train-labels sont présents.",
            filePath
        ));
        this.missingFilePath = filePath;
    }

    public MNISTFileNotFoundException(String filePath, Throwable cause) {
        super(String.format(
            "Fichier MNIST introuvable ou illisible : '%s'. " +
            "Vérifiez que les fichiers train-images et train-labels sont présents.",
            filePath
        ), cause);
        this.missingFilePath = filePath;
    }

    public String getMissingFilePath() { return missingFilePath; }
}
