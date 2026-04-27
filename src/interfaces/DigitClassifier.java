package interfaces;

/**
 * Interface pour les classifieurs de chiffres manuscrits.
 * Le polymorphisme permet de basculer entre les modèles sans modifier le code client.
 */
public interface DigitClassifier {
    /**
     * Prédit le label d'un vecteur de pixels.
     * @param pixelVector vecteur de 784 valeurs d'intensité (0-255)
     * @return le label prédit : "trois" ou "cinq"
     */
    String predict(int[] pixelVector) throws Exception;

    /**
     * Retourne la probabilité de la prédiction (si disponible).
     * @param pixelVector vecteur de 784 valeurs d'intensité
     * @return probabilité entre 0.0 et 1.0
     */
    double getProbability(int[] pixelVector) throws Exception;
}
