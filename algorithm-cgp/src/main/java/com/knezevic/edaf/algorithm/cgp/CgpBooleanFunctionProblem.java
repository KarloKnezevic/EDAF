package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.core.api.OptimizationType;

import java.util.Map;

public class CgpBooleanFunctionProblem implements CgpProblem {

    // 3-input XOR
    private final boolean[][] inputs = {
        {false, false, false},
        {false, false, true},
        {false, true, false},
        {false, true, true},
        {true, false, false},
        {true, false, true},
        {true, true, false},
        {true, true, true}
    };
    private final boolean[] outputs = {false, true, true, false, true, false, false, true};

    @Override
    public int getNumInputs() {
        return 3;
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

        int errors = 0;
        for (int i = 0; i < inputs.length; i++) {
            double[] doubleInputs = new double[inputs[i].length];
            for (int j = 0; j < inputs[i].length; j++) {
                doubleInputs[j] = inputs[i][j] ? 1.0 : 0.0;
            }
            double[] actual = individual.getPhenotype().execute(doubleInputs);
            boolean actualBool = actual[0] >= 0.5;
            if (actualBool != outputs[i]) {
                errors++;
            }
        }
        individual.setFitness((double) errors);
    }

    @Override
    public OptimizationType getOptimizationType() {
        return OptimizationType.MINIMIZE;
    }
}
