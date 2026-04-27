package classifier;

import interfaces.DigitClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;

/**
 * Classifieur Naive Bayes encapsulant weka.classifiers.bayes.NaiveBayes.
 *
 * Utilisation :
 *   NaiveBayesClassifier nb = new NaiveBayesClassifier("train-data.arff");
 *   String result = nb.predict(pixelVector);
 */
public class NaiveBayesClassifier implements DigitClassifier {

    private NaiveBayes model;
    private Instances  trainData;
    private String     arffPath;

    // Attributs Weka partagés (même structure que les données d'entraînement)
    private ArrayList<Attribute> attributes;

    // ------------------------------------------------------------------ //
    //  Constructeur
    // ------------------------------------------------------------------ //

    /**
     * Charge le fichier ARFF et entraîne le modèle Naive Bayes.
     *
     * @param arffPath chemin vers le fichier .arff d'entraînement
     */
    public NaiveBayesClassifier(String arffPath) throws Exception {
        this.arffPath = arffPath;
        DataSource source = new DataSource(arffPath);
        trainData = source.getDataSet();
        trainData.setClassIndex(trainData.numAttributes() - 1);

        this.attributes = buildAttributes();

        model = new NaiveBayes();
        model.buildClassifier(trainData);
        System.out.println("NaiveBayes entraîné sur " + trainData.numInstances()
                + " exemples (" + arffPath + ")");
    }

    // ------------------------------------------------------------------ //
    //  DigitClassifier
    // ------------------------------------------------------------------ //

    @Override
    public String predict(int[] pixelVector) throws Exception {
        Instance instance = buildInstance(pixelVector);
        double classIndex = model.classifyInstance(instance);
        return instance.classAttribute().value((int) classIndex);
    }

    @Override
    public double getProbability(int[] pixelVector) throws Exception {
        Instance instance = buildInstance(pixelVector);
        double[] dist = model.distributionForInstance(instance);
        // Retourne la probabilité maximale
        double max = 0;
        for (double d : dist) max = Math.max(max, d);
        return max;
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private Instance buildInstance(int[] pixelVector) {
        Instances dataset = new Instances("predict", attributes, 1);
        dataset.setClassIndex(dataset.numAttributes() - 1);

        double[] values = new double[attributes.size()];
        for (int i = 0; i < 784; i++) {
            values[i] = pixelVector[i];
        }
        values[784] = 0; // valeur de classe arbitraire (sera ignorée)

        DenseInstance inst = new DenseInstance(1.0, values);
        inst.setDataset(dataset);
        return inst;
    }

    private ArrayList<Attribute> buildAttributes() {
        ArrayList<Attribute> attrs = new ArrayList<>();
        for (int i = 0; i < 784; i++) {
            attrs.add(new Attribute("pixel" + i));
        }
        ArrayList<String> classValues = new ArrayList<>();
        classValues.add("trois");
        classValues.add("cinq");
        attrs.add(new Attribute("label", classValues));
        return attrs;
    }

    public String getArffPath() { return arffPath; }
    public NaiveBayes getModel() { return model; }
    public Instances  getTrainData() { return trainData; }
}
