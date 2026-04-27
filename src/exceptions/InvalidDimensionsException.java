package exceptions;

/**
 * Exception levée lorsque l'image fournie n'est pas de dimensions exactes 28×28 pixels.
 */
public class InvalidDimensionsException extends Exception {

    private final int actualWidth;
    private final int actualHeight;

    /**
     * @param actualWidth  largeur réelle détectée
     * @param actualHeight hauteur réelle détectée
     */
    public InvalidDimensionsException(int actualWidth, int actualHeight) {
        super(String.format(
            "Dimensions invalides : l'image doit être 28×28 pixels, " +
            "mais les dimensions détectées sont %d×%d pixels.",
            actualWidth, actualHeight
        ));
        this.actualWidth  = actualWidth;
        this.actualHeight = actualHeight;
    }

    public InvalidDimensionsException(int actualWidth, int actualHeight, Throwable cause) {
        super(String.format(
            "Dimensions invalides : l'image doit être 28×28 pixels, " +
            "mais les dimensions détectées sont %d×%d pixels.",
            actualWidth, actualHeight
        ), cause);
        this.actualWidth  = actualWidth;
        this.actualHeight = actualHeight;
    }

    public int getActualWidth()  { return actualWidth; }
    public int getActualHeight() { return actualHeight; }
}
