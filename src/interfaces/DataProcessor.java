package interfaces;

/**
 * Interface définissant les contrats de base pour le traitement de données MNIST.
 */
public interface DataProcessor {
    /**
     * Charge les données depuis une source.
     */
    void load() throws Exception;

    /**
     * Exporte les données vers une destination.
     * @param destination chemin ou nom du fichier de sortie
     */
    void export(String destination) throws Exception;
}
