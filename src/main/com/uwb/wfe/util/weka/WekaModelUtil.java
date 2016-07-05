package com.uwb.wfe.util.weka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Weka is a data-mining tool developed by the University of Waikato. This util uses the Weka API to translate
 * the *.arff file generated by the "training" Python script into a usable model.
 *
 * Created by milesdowe on 7/4/16.
 */
@Service
public class WekaModelUtil {

    // copied from the Weka app when building the model manually
    private static final String KNN_OPTIONS = "-K 5 -W 0 -A \"weka.core.neighboursearch.LinearNNSearch -A " +
                                              "\\\"weka.core.EuclideanDistance -R first-last\\\"\"";

    @Value("${training.model.path}")
    private String modelOutputPath;

    private Instances data;

    public void readArffFile(String filepath) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(filepath);
        data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);
    }

    public void classifyAndSaveModel() throws Exception {
        // set classifier
        IBk iBk = new IBk();

        // establish algorithm options
        iBk.setOptions(weka.core.Utils.splitOptions(KNN_OPTIONS));

        // train
        iBk.buildClassifier(data);

        // serialize as *.model file
        saveModel(iBk);
    }

    private void saveModel(Classifier classifier) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelOutputPath));
        oos.writeObject(classifier);
        oos.flush();
        oos.close();
    }
}
