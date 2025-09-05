package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.core.api.OptimizationType;

import java.util.Map;

public class CgpIrisClassificationProblem implements CgpProblem {

    // A small, hardcoded subset of the Iris dataset.
    // {sepal_length, sepal_width, petal_length, petal_width, species}
    private static final double[][] DATA = {
        {5.1, 3.5, 1.4, 0.2, 0}, // setosa
        {4.9, 3.0, 1.4, 0.2, 0},
        {4.7, 3.2, 1.3, 0.2, 0},
        {7.0, 3.2, 4.7, 1.4, 1}, // versicolor
        {6.4, 3.2, 4.5, 1.5, 1},
        {6.9, 3.1, 4.9, 1.5, 1},
        {6.3, 3.3, 6.0, 2.5, 2}, // virginica
        {5.8, 2.7, 5.1, 1.9, 2},
        {7.1, 3.0, 5.9, 2.1, 2},
    };

    @Override
    public int getNumInputs() {
        return 4;
    }

    @Override
    public int getNumOutputs() {
        return 1;
    }

    @Override
    public void evaluate(CgpIndividual individual) {
        if (individual.getPhenotype() == null) {
            individual.setFitness(Double.MAX_VALUE);
            return;
        }

        int misclassified = 0;
        for (double[] dataPoint : DATA) {
            double[] inputs = {dataPoint[0], dataPoint[1], dataPoint[2], dataPoint[3]};
            double expectedClass = dataPoint[4];
            double[] rawOutput = individual.getPhenotype().execute(inputs);

            // Clamp and round the output to get a class prediction
            long predictedClass = Math.round(Math.max(0, Math.min(2, rawOutput[0])));

            if (predictedClass != (long) expectedClass) {
                misclassified++;
            }
        }
        individual.setFitness(misclassified);
    }

    @Override
    public OptimizationType getOptimizationType() {
        return OptimizationType.MINIMIZE;
    }
}
