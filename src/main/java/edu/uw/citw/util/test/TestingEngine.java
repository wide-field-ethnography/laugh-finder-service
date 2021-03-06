package edu.uw.citw.util.test;

import edu.uw.citw.persistence.domain.ModelData;
import edu.uw.citw.persistence.repository.ModelDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for reading *.arff and *.index files generated by PyLaughFinderUtil, and the current
 *   model provide laughter timestamps.
 * <p>
 * Copied from Shalini's code.
 * <p>
 * Created by milesdowe on 7/12/16.
 */
@Component("testEngine")
public class TestingEngine {

    private static final Logger log = LoggerFactory.getLogger(TestingEngine.class);

    @Value("${testing.index.path}")
    private String indexPath;
    @Value("${training.model.path}")
    private String modelPath;
    @Value("${testing.window}")
    private String windowSize;
    @Value("${testing.arff.path}")
    private String arffPath;

    private Instances     instances;
    private List<Boolean> isPresentList;

    private ModelDataRepository modelDataRepository;

    public TestingEngine(ModelDataRepository modelDataRepository) {
        this.modelDataRepository = modelDataRepository;
    }

    /**
     * Get the laughter segments from the ARFF files.
     */
    public List<long[]> getLaughters() throws Exception {
        isPresentList = getIsPresentList(indexPath);
        List<long[]> laughtersInMilliSecs = new ArrayList<>();

        try {
            log.debug("Getting instances out of ARFF file from python test script.");
            BufferedReader arffReader = new BufferedReader(new FileReader(this.arffPath));
            instances = new Instances(arffReader);
            instances.setClassIndex(instances.numAttributes() - 1);

            log.debug("Getting binary of model currently in use.");
            List<ModelData> modelBytes = modelDataRepository.findByInUse(true);

            log.debug("Reading binary of model.");
            Classifier model = (Classifier) weka.core.SerializationHelper
                    .read(
                        new ByteArrayInputStream(modelBytes.get(0).getModelBinary())
                    );

            log.debug("Examining the ARFF instances.");
            Evaluation test        = new Evaluation(instances);
            double[]   predictions = test.evaluateModel(model, instances);

            boolean[] isLaughterList = this.merge(predictions, isPresentList);

            log.debug("Grabbing laughter time frames.");
            int start, end;

            for (int i = 0; i < isLaughterList.length; i++) {

                // TODO: using "baby giggle" sound, get impossible timeframe (i.e., 0:11.200 to 0:10.400)
                // TODO: false negatives, not recognizing baby giggling (probably unimportant, bad sample)
                if (isLaughterList[i]) {
                    start = i;
                    while (isLaughterList[i] && i < isLaughterList.length - 1) {
                        i++;
                    }
                    end = i;
                    if (end != start + 1) {
                        end--;
                    }
                    long window = Long.parseLong(windowSize);
                    laughtersInMilliSecs.add(new long[] {
                            start * window,
                            end * window
                    });
                    log.debug("{} to {}",
                        getDisplayTime(start * window),
                        getDisplayTime(end * window)
                    );
                }
            }
            arffReader.close();
        } catch (Exception e) {
            // unfortunate that the WEKA API uses generic Exception...
            log.error("There was a problem", e);
        }
        return laughtersInMilliSecs;
    }

    /**
     * The WEKA instances parsed from the ARFF file.
     */
    public Instances getInstances() {
        return this.instances;
    }

    /**
     * The list of boolean value indicating if the instances in the ARFF file
     * was returned as a laughter segment.
     */
    public List<Boolean> isPresentList() {
        return this.isPresentList;
    }

    /**
     * Merges the predictions with existing instances.
     */
    private boolean[] merge(double[] predictions, List<Boolean> isPresentList) {
        boolean[] isLaughterList   = new boolean[isPresentList.size()];
        int       predictionsIndex = 0;

        for (int i = 0; i < isLaughterList.length; i++) {
            if (isPresentList.get(i)) {
                if (predictions[predictionsIndex] == 0) {
                    isLaughterList[i] = true;
                }
                predictionsIndex++;
            }
        }

        return isLaughterList;
    }

    /**
     * Produces a list of booleans for each instance from the test set
     * indicating if they were identified as laughter.
     */
    private List<Boolean> getIsPresentList(String indexFileName) {
        List<Boolean>  isPresentList = new ArrayList<Boolean>();
        BufferedReader indexFileReader;
        try {
            indexFileReader = new BufferedReader(new FileReader(indexFileName));
            String line;
            while ((line = indexFileReader.readLine()) != null) {
                isPresentList.add(line.trim().equalsIgnoreCase("YES"));
            }
            indexFileReader.close();
        } catch (IOException e) {
            // TODO: handle this better?
            log.error("There was an error", e);
        }

        return isPresentList;
    }

    /**
     * Get the human readable display time as a string.
     */
    private String getDisplayTime(long timeInMilliseconds) {
        double seconds = timeInMilliseconds / 1000.0;
        int    minute  = (int) (seconds / 60);
        double second  = seconds % 60;
        return minute + ":" + String.format("%.3f", second);
    }

    public String getArffPath() {
        return arffPath;
    }

    public void setArffPath(String arffPath) {
        this.arffPath = arffPath;
    }
}
