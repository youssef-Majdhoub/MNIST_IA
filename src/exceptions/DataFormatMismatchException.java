package exceptions;

/**
 * Exception levée lorsqu'une ligne du fichier CSV ne respecte pas le format attendu :
 * 784 entiers + 1 label = 785 champs.
 */
public class DataFormatMismatchException extends Exception {

    private final int    lineNumber;
    private final String problematicValue;

    /**
     * @param lineNumber       numéro de la ligne fautive (1-indexé)
     * @param problematicValue valeur qui a causé l'erreur
     * @param detail           description précise du problème
     */
    public DataFormatMismatchException(int lineNumber, String problematicValue, String detail) {
        super(String.format(
            "Format invalide à la ligne %d — valeur problématique : '%s'. %s",
            lineNumber, problematicValue, detail
        ));
        this.lineNumber       = lineNumber;
        this.problematicValue = problematicValue;
    }

    public DataFormatMismatchException(int lineNumber, String problematicValue,
                                       String detail, Throwable cause) {
        super(String.format(
            "Format invalide à la ligne %d — valeur problématique : '%s'. %s",
            lineNumber, problematicValue, detail
        ), cause);
        this.lineNumber       = lineNumber;
        this.problematicValue = problematicValue;
    }

    public int    getLineNumber()       { return lineNumber; }
    public String getProblematicValue() { return problematicValue; }
}
