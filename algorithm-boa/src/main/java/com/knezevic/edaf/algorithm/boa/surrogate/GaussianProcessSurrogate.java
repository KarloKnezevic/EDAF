package com.knezevic.edaf.algorithm.boa.surrogate;

import weka.classifiers.functions.GaussianProcesses;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class GaussianProcessSurrogate {
    private GaussianProcesses gp = new GaussianProcesses();
    private Instances data;

    public void fit(Instances data) throws Exception {
        this.data = data;
        this.data.setClassIndex(data.numAttributes() - 1);
        gp.buildClassifier(data);
    }

    public double[] predict(Instance instance) throws Exception {
        double[] result = new double[2];
        result[0] = gp.distributionForInstance(instance)[0];
        result[1] = gp.getStandardDeviation(instance);
        return result;
    }
}
