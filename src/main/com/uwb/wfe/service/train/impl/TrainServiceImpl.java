package com.uwb.wfe.service.train.impl;

import com.uwb.wfe.service.train.TrainService;
import com.uwb.wfe.util.weka.WekaModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Allows users to train the model.
 *
 * Created by Miles on 7/3/2016.
 */
@Service
public class TrainServiceImpl implements TrainService {

    private static Logger log = LoggerFactory.getLogger(TrainServiceImpl.class);

    WekaModelUtil wekaUtil;

    // supervised learning samples and resulting ARFF file
    @Value("${training.laughterSamples}")
    private String laughterSampleLocation;
    @Value("${training.nonLaughterSamples}")
    private String nonLaughterSampleLocation;
    @Value("${training.arff.path}")
    private String outputLocation;

    @Value("${training.program.location}")
    private String learningScriptLocation;

    @Value("${python.nix.location}")
    private String pythonLocation;

    @Autowired
    public TrainServiceImpl(WekaModelUtil wekaUtil) {
        this.wekaUtil = wekaUtil;
    }

    @Override
    public void trainModel() throws InterruptedException, IOException {
        log.info("Training model...");

        String[] cmd = {
                pythonLocation,
                learningScriptLocation,
                laughterSampleLocation,
                nonLaughterSampleLocation,
                outputLocation
        };

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process proc = pb.start();
        proc.waitFor();

        if (proc.exitValue() != 0)
            log.warn("There was a failure training the model");
        else
            log.info("Training complete");

        try {
            wekaUtil.readArffFile(outputLocation);
            wekaUtil.classifyAndSaveModel();
        } catch (Exception e) {
            log.error("There was a failure converting the .arff file to a .model file", e);
        }
    }
}
