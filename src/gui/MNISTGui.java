package gui;

import classifier.NaiveBayesClassifier;
import classifier.RandomForestClassifier;
import interfaces.DigitClassifier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Interface graphique Swing pour la reconnaissance de chiffres manuscrits.
 *
 * Composants :
 *  - Canevas de dessin 280×280 px (redimensionné à 28×28 pour la reconnaissance)
 *  - Bouton « Effacer »
 *  - Sélecteur de modèle (JComboBox : Naive Bayes / Random Forest)
 *  - Bouton « Reconnaître »
 *  - Zone de résultat (label prédit + probabilité)
 */
public class MNISTGui extends JFrame {

    // ---- Dimensions ----
    private static final int CANVAS_SIZE  = 280;
    private static final int IMG_SIZE     = 28;
    private static final int STROKE_WIDTH = 18;

    // ---- Composants ----
    private DrawingCanvas    canvas;
    private JComboBox<String> modelComboBox;
    private JLabel           resultLabel;
    private JLabel           probLabel;
    private JLabel           statusLabel;
    private JButton          recognizeBtn;
    private JButton          clearBtn;

    // ---- Classifieurs ----
    private DigitClassifier naiveBayes;
    private DigitClassifier randomForest;
    private boolean         modelsLoaded = false;
    private String          arffPath;

    // ------------------------------------------------------------------ //
    //  Constructeur
    // ------------------------------------------------------------------ //

    public MNISTGui(String arffPath) {
        this.arffPath = arffPath;
        initUI();
        loadModelsAsync();
    }

    // ------------------------------------------------------------------ //
    //  Construction de l'interface
    // ------------------------------------------------------------------ //

    private void initUI() {
        setTitle("Reconnaissance de Chiffres Manuscrits — MNIST");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // ---- Panneau principal ----
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 242, 245));

        // ---- Titre ----
        JLabel titleLabel = new JLabel("Reconnaissance : Trois ou Cinq ?", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(30, 30, 80));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // ---- Canevas ----
        canvas = new DrawingCanvas();
        canvas.setPreferredSize(new Dimension(CANVAS_SIZE, CANVAS_SIZE));
        canvas.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 200), 2));

        JPanel canvasWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        canvasWrapper.setOpaque(false);
        canvasWrapper.add(canvas);
        mainPanel.add(canvasWrapper, BorderLayout.CENTER);

        // ---- Panneau de contrôle (droite) ----
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setOpaque(false);
        controlPanel.setPreferredSize(new Dimension(220, CANVAS_SIZE));
        controlPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        // Sélecteur de modèle
        JLabel modelLabel = new JLabel("Modèle :");
        modelLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        modelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        modelComboBox = new JComboBox<>(new String[]{"Naive Bayes", "Random Forest"});
        modelComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        modelComboBox.setMaximumSize(new Dimension(200, 35));
        modelComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Bouton Reconnaître
        recognizeBtn = createStyledButton("🔍  Reconnaître", new Color(60, 100, 200));
        recognizeBtn.addActionListener(e -> recognize());
        recognizeBtn.setEnabled(false);

        // Bouton Effacer
        clearBtn = createStyledButton("🗑   Effacer", new Color(180, 60, 60));
        clearBtn.addActionListener(e -> canvas.clear());

        // Zone de résultat
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(new Color(255, 255, 255, 200));
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 220), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        resultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.setMaximumSize(new Dimension(200, 120));

        JLabel resultTitle = new JLabel("Résultat :");
        resultTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resultTitle.setForeground(new Color(80, 80, 80));

        resultLabel = new JLabel("—");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        resultLabel.setForeground(new Color(30, 30, 120));

        probLabel = new JLabel("Probabilité : —");
        probLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        probLabel.setForeground(new Color(100, 100, 100));

        resultPanel.add(resultTitle);
        resultPanel.add(Box.createVerticalStrut(5));
        resultPanel.add(resultLabel);
        resultPanel.add(Box.createVerticalStrut(3));
        resultPanel.add(probLabel);

        // Status bar
        statusLabel = new JLabel("Chargement des modèles...");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(new Color(120, 120, 120));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Assemblage du panneau de contrôle
        controlPanel.add(modelLabel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(modelComboBox);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(recognizeBtn);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(clearBtn);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(resultPanel);
        controlPanel.add(Box.createVerticalGlue());
        controlPanel.add(statusLabel);

        mainPanel.add(controlPanel, BorderLayout.EAST);

        // Instruction
        JLabel hint = new JLabel("Dessinez un 3 ou un 5 dans la zone de gauche, puis cliquez sur « Reconnaître ».",
                SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hint.setForeground(new Color(100, 100, 130));
        mainPanel.add(hint, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ------------------------------------------------------------------ //
    //  Chargement asynchrone des modèles
    // ------------------------------------------------------------------ //

    private void loadModelsAsync() {
        new Thread(() -> {
            try {
                naiveBayes   = new NaiveBayesClassifier(arffPath);
                randomForest = new RandomForestClassifier(arffPath);
                modelsLoaded = true;
                SwingUtilities.invokeLater(() -> {
                    recognizeBtn.setEnabled(true);
                    statusLabel.setText("Modèles prêts ✓");
                    statusLabel.setForeground(new Color(0, 130, 0));
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Erreur : " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(MNISTGui.this,
                            "Impossible de charger les modèles :\n" + e.getMessage()
                            + "\n\nVérifiez que le fichier ARFF existe : " + arffPath,
                            "Erreur de chargement", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    // ------------------------------------------------------------------ //
    //  Reconnaissance
    // ------------------------------------------------------------------ //

    private void recognize() {
        if (!modelsLoaded) {
            JOptionPane.showMessageDialog(this,
                    "Les modèles sont encore en cours de chargement. Patientez...",
                    "Patientez", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int[] pixelVector = canvas.toPixelVector();
        String selected = (String) modelComboBox.getSelectedItem();

        try {
            DigitClassifier clf = "Naive Bayes".equals(selected) ? naiveBayes : randomForest;
            String prediction   = clf.predict(pixelVector);
            double probability  = clf.getProbability(pixelVector);

            resultLabel.setText(prediction.toUpperCase());
            probLabel.setText(String.format("Probabilité : %.1f %%", probability * 100));

            Color color = "trois".equals(prediction)
                    ? new Color(20, 100, 200)
                    : new Color(180, 80, 0);
            resultLabel.setForeground(color);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la reconnaissance :\n" + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================================================================== //
    //  Canevas de dessin
    // ================================================================== //

    private static class DrawingCanvas extends JPanel {

        private BufferedImage image;
        private Graphics2D    g2;

        DrawingCanvas() {
            setBackground(Color.WHITE);
            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    draw(e.getX(), e.getY());
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseDragged(MouseEvent e) {
                    draw(e.getX(), e.getY());
                }
            });
        }

        private void ensureImage() {
            if (image == null) {
                image = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_RGB);
                g2 = image.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                clear();
            }
        }

        void draw(int x, int y) {
            ensureImage();
            g2.setColor(Color.BLACK);
            g2.fillOval(x - STROKE_WIDTH / 2, y - STROKE_WIDTH / 2,
                        STROKE_WIDTH, STROKE_WIDTH);
            repaint();
        }

        void clear() {
            ensureImage();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
            repaint();
            // Réinitialise aussi les labels du parent si accessible
        }

        /**
         * Redimensionne le canevas 280×280 à 28×28 et retourne le vecteur de pixels.
         */
        int[] toPixelVector() {
            ensureImage();
            BufferedImage small = new BufferedImage(IMG_SIZE, IMG_SIZE,
                                                    BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D gs = small.createGraphics();
            gs.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gs.drawImage(image, 0, 0, IMG_SIZE, IMG_SIZE, null);
            gs.dispose();

            int[] pixels = new int[IMG_SIZE * IMG_SIZE];
            for (int y = 0; y < IMG_SIZE; y++) {
                for (int x = 0; x < IMG_SIZE; x++) {
                    // MNIST : blanc = 0, noir = 255 (inverse de l'écran)
                    int rgb  = small.getRGB(x, y);
                    int grey = (rgb >> 16) & 0xFF;          // canal rouge du gris
                    pixels[y * IMG_SIZE + x] = 255 - grey;  // inversion
                }
            }
            return pixels;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            ensureImage();
            g.drawImage(image, 0, 0, null);
        }
    }

    // ------------------------------------------------------------------ //
    //  Point d'entrée
    // ------------------------------------------------------------------ //

    public static void main(String[] args) {
        String arffPath = "data/train-data.arff";
        if (args.length > 0) arffPath = args[0];

        final String path = arffPath;
        SwingUtilities.invokeLater(() -> new MNISTGui(path));
    }
}
