package com.knezevic.edaf.algorithm.cgp.problems;

import com.knezevic.edaf.algorithm.cgp.CgpIndividual;
import com.knezevic.edaf.algorithm.cgp.CgpProblem;
import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.genotype.tree.primitives.Function;

import java.util.List;
import java.util.Map;

/**
 * Parity problem for CGP.
 * Classic GP benchmark: output 1 if odd number of inputs are 1, else 0.
 */
public class CgpParityProblem implements CgpProblem {
    private final int n;
    private final List<Function> functionSet;

    public CgpParityProblem(Map<String, Object> params) {
        this.n = 4; // 4-bit parity
        
        // Boolean function set for parity
        this.functionSet = List.of(
            new Function("AND", 2, args -> (args[0] >= 0.5 && args[1] >= 0.5) ? 1.0 : 0.0),
            new Function("OR", 2, args -> (args[0] >= 0.5 || args[1] >= 0.5) ? 1.0 : 0.0),
            new Function("NOT", 1, args -> (args[0] >= 0.5) ? 0.0 : 1.0),
            new Function("XOR", 2, args -> ((args[0] >= 0.5) != (args[1] >= 0.5)) ? 1.0 : 0.0),
            new Function("NAND", 2, args -> (!(args[0] >= 0.5 && args[1] >= 0.5)) ? 1.0 : 0.0)
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
            individual.setFitness(Double.MAX_VALUE);
            return;
        }

        int errors = 0;
        for (int i = 0; i < (1 << n); i++) {
            double[] inputs = new double[n];
            int onesCount = 0;
            for (int j = 0; j < n; j++) {
                int bit = (i >> j) & 1;
                inputs[j] = bit == 1 ? 1.0 : 0.0;
                onesCount += bit;
            }
            
            // Expected output: 1 if odd number of ones, 0 if even
            double expected = (onesCount % 2 == 1) ? 1.0 : 0.0;
            
            double[] outputs = individual.getPhenotype().execute(inputs);
            double actual = (outputs[0] >= 0.5) ? 1.0 : 0.0;
            
            if (Math.abs(actual - expected) > 0.01) {
                errors++;
            }
        }
        
        individual.setFitness(errors);
    }

    @Override
    public OptimizationType getOptimizationType() {
        return OptimizationType.min; // Minimize errors
    }

    @Override
    public List<Function> getFunctionSet() {
        return functionSet;
    }
}

