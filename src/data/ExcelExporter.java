package data;

import exceptions.DataFormatMismatchException;
import exceptions.MNISTFileNotFoundException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.List;

/**
 * Exporte les données MNIST vers un fichier Excel (.xlsx) en utilisant Apache POI.
 *
 * Structure du classeur :
 *   - 1 feuille « MNIST »
 *   - En-têtes : pixel0, pixel1, …, pixel783, label
 *   - 1 ligne par exemple
 */
public class ExcelExporter extends MNISTProvider {

    private String sourceFilePath;  // Fichier CSV source
    private String outputPath;       // Fichier Excel de destination

    private List<int[]>   pixels;
    private List<Integer> labels;

    // ------------------------------------------------------------------ //
    //  Constructeurs
    // ------------------------------------------------------------------ //

    /** Constructeur à partir d'un fichier CSV. */
    public ExcelExporter(String sourceFilePath) {
        super();
        this.sourceFilePath = sourceFilePath;
    }

    /** Constructeur à partir de données déjà chargées. */
    public ExcelExporter(List<int[]> pixels, List<Integer> labels) {
        super(pixels.size());
        this.pixels = pixels;
        this.labels = labels;
    }

    // ------------------------------------------------------------------ //
    //  DataProcessor
    // ------------------------------------------------------------------ //

    @Override
    public void load() throws DataFormatMismatchException, MNISTFileNotFoundException {
        if (sourceFilePath == null) {
            throw new IllegalStateException("Aucun fichier source défini.");
        }
        TextFileHandler handler = new TextFileHandler(sourceFilePath);
        handler.load();
        this.pixels   = handler.getPixels();
        this.labels   = handler.getLabels();
        this.nbImages = pixels.size();
    }

    @Override
    public void export(String destination) throws IOException {
        if (pixels == null || pixels.isEmpty()) {
            throw new IllegalStateException("Aucune donnée à exporter. Appelez load() d'abord.");
        }
        createExcelFile(destination, pixels, labels);
        this.outputPath = destination;
    }

    // ------------------------------------------------------------------ //
    //  Méthode principale d'export Excel
    // ------------------------------------------------------------------ //

    /**
     * Crée un fichier Excel à partir d'un fichier texte CSV.
     *
     * @param nomFichierCsv  chemin du fichier CSV source
     * @param nomFichierXlsx chemin du fichier Excel à générer
     */
    public static void createExcelFile(String nomFichierCsv, String nomFichierXlsx)
            throws Exception {

        TextFileHandler handler = new TextFileHandler(nomFichierCsv);
        handler.load();
        createExcelFile(nomFichierXlsx, handler.getPixels(), handler.getLabels());
    }

    /**
     * Version interne : écrit directement depuis des listes en mémoire.
     */
    public static void createExcelFile(String outputPath,
                                        List<int[]>   pixels,
                                        List<Integer> labels) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("MNIST");

            // --- Ligne d'en-tête ---
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < VECTOR_SIZE; col++) {
                headerRow.createCell(col).setCellValue("pixel" + col);
            }
            headerRow.createCell(VECTOR_SIZE).setCellValue("label");

            // Style pour les en-têtes
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }

            // --- Lignes de données ---
            for (int rowIdx = 0; rowIdx < pixels.size(); rowIdx++) {
                Row row = sheet.createRow(rowIdx + 1);
                int[] pixelRow = pixels.get(rowIdx);

                for (int col = 0; col < VECTOR_SIZE; col++) {
                    row.createCell(col).setCellValue(pixelRow[col]);
                }

                String frenchLabel = (labels.get(rowIdx) == 3) ? "trois" : "cinq";
                row.createCell(VECTOR_SIZE).setCellValue(frenchLabel);
            }

            // --- Sauvegarde ---
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        }

        System.out.println("createExcelFile → " + outputPath
                + " (" + pixels.size() + " lignes de données + 1 en-tête)");
    }

    // ------------------------------------------------------------------ //
    //  Accesseurs
    // ------------------------------------------------------------------ //

    public String getOutputPath() { return outputPath; }
    public List<int[]>   getPixels() { return pixels; }
    public List<Integer> getLabels() { return labels; }
}
