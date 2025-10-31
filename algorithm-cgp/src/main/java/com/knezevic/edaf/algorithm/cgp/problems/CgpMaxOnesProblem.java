package com.knezevic.edaf.algorithm.cgp.problems;

import com.knezevic.edaf.algorithm.cgp.CgpIndividual;
import com.knezevic.edaf.algorithm.cgp.CgpProblem;
import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.genotype.tree.primitives.Function;

import java.util.List;
import java.util.Map;

/**
 * MaxOnes problem for CGP.
 * Tries to create a function that outputs 1 for as many input combinations as possible.
 */
public class CgpMaxOnesProblem implements CgpProblem {
    private final int n;
    private final List<Function> functionSet;

    public CgpMaxOnesProblem(Map<String, Object> params) {
        this.n = 4; // 4 binary inputs
        
        // Simple boolean function set
        this.functionSet = List.of(
            new Function("AND", 2, args -> (args[0] >= 0.5 && args[1] >= 0.5) ? 1.0 : 0.0),
            new Function("OR", 2, args -> (args[0] >= 0.5 || args[1] >= 0.5) ? 1.0 : 0.0),
            new Function("NOT", 1, args -> (args[0] >= 0.5) ? 0.0 : 1.0),
            new Function("XOR", 2, args -> ((args[0] >= 0.5) != (args[1] >= 0.5)) ? 1.0 : 0.0)
        );
    }

    @Override
    public int getNumInputs() {
        return n;
    }

    @Override
    public int getNumOutputs() {
        return 1;
    }

    @Override
    public void evaluate(CgpIndividual individual) {
        if (individual.getPhenotype() == null) {
            individual.setFitness(0.0);
            return;
        }

        int ones = 0;
        for (int i = 0; i < (1 << n); i++) {
            double[] inputs = new double[n];
            for (int j = 0; j < n; j++) {
                inputs[j] = ((i >> j) & 1) == 1 ? 1.0 : 0.0;
            }
            double[] outputs = individual.getPhenotype().execute(inputs);
            if (outputs[0] >= 0.5) {
                ones++;
            }
        }
        
        // Maximize the number of ones
        individual.setFitness(ones);
    }

    @Override
    public OptimizationType getOptimizationType() {
        return OptimizationType.max;
    }

    @Override
    public List<Function> getFunctionSet() {
        return functionSet;
    }
}

