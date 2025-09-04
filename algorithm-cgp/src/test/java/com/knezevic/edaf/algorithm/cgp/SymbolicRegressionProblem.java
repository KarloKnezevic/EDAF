package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.core.api.OptimizationType;

import java.util.Map;
import java.util.stream.IntStream;

public class SymbolicRegressionProblem implements CgpProblem {

    private final Map<Double, Double> data;

    public SymbolicRegressionProblem() {
        // y = x^2 + x + 1
        this.data = Map.of(
            -1.0, 1.0,
            0.0, 1.0,
            1.0, 3.0,
            2.0, 7.0,
            3.0, 13.0
        );
    }

    @Override
    public int getNumInputs() {
        return 1;
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

        double error = 0.0;
        for (Map.Entry<Double, Double> entry : data.entrySet()) {
            double[] inputs = {entry.getKey()};
            double expected = entry.getValue();
            double[] actual = individual.getPhenotype().execute(inputs);
            error += Math.pow(expected - actual[0], 2);
        }

        individual.setFitness(error / data.size()); // Mean Squared Error
    }

    @Override
    public OptimizationType getOptimizationType() {
        return OptimizationType.MINIMIZE;
    }
}
