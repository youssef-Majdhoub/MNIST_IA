package classifier;

import interfaces.DigitClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;

/**
 * Classifieur Random Forest encapsulant weka.classifiers.trees.RandomForest.
 *
 * Utilisation :
 *   RandomForestClassifier rf = new RandomForestClassifier("train-data.arff");
 *   String result = rf.predict(pixelVector);
 */
public class RandomForestClassifier implements DigitClassifier {

    private RandomForest model;
    private Instances    trainData;
    private String       arffPath;

    private ArrayList<Attribute> attributes;

    // ------------------------------------------------------------------ //
    //  Constructeur
    // ------------------------------------------------------------------ //

    /**
     * Charge le fichier ARFF et entraîne le modèle Random Forest.
     *
     * @param arffPath chemin vers le fichier .arff d'entraînement
     */
    public RandomForestClassifier(String arffPath) throws Exception {
        this.arffPath = arffPath;
        DataSource source = new DataSource(arffPath);
        trainData = source.getDataSet();
        trainData.setClassIndex(trainData.numAttributes() - 1);

        this.attributes = buildAttributes();

        model = new RandomForest();
        model.setNumIterations(100);   // 100 arbres
        model.setNumFeatures(28);      // sqrt(784) ≈ 28 features par split
        model.buildClassifier(trainData);
        System.out.println("RandomForest entraîné sur " + trainData.numInstances()
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
        values[784] = 0;

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

    public String       getArffPath()  { return arffPath; }
    public RandomForest getModel()     { return model; }
    public Instances    getTrainData() { return trainData; }
}
