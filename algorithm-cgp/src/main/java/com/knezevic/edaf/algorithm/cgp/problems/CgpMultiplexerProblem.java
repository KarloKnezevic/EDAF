package com.knezevic.edaf.algorithm.cgp.problems;

import com.knezevic.edaf.algorithm.cgp.CgpIndividual;
import com.knezevic.edaf.algorithm.cgp.CgpProblem;
import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.genotype.tree.primitives.Function;

import java.util.List;
import java.util.Map;

/**
 * 6-bit Multiplexer problem for CGP.
 * Inputs: A1, A0 (address bits), D0, D1, D2, D3 (data bits)
 * Output: Selected data bit based on address
 */
public class CgpMultiplexerProblem implements CgpProblem {
    private final List<Function> functionSet;

    public CgpMultiplexerProblem(Map<String, Object> params) {
        // Boolean function set for multiplexer
        this.functionSet = List.of(
            new Function("AND", 2, args -> (args[0] >= 0.5 && args[1] >= 0.5) ? 1.0 : 0.0),
            new Function("OR", 2, args -> (args[0] >= 0.5 || args[1] >= 0.5) ? 1.0 : 0.0),
            new Function("NOT", 1, args -> (args[0] >= 0.5) ? 0.0 : 1.0),
            new Function("XOR", 2, args -> ((args[0] >= 0.5) != (args[1] >= 0.5)) ? 1.0 : 0.0),
            new Function("IF", 3, args -> (args[0] >= 0.5) ? args[1] : args[2])
        );
    }

    @Override
    public int getNumInputs() {
        return 6; // A1, A0, D3, D2, D1, D0
    }

    @Override
    public int getNumOutputs() {
        return 1; // Output bit
    }

    @Override
    public void evaluate(CgpIndividual individual) {
        if (individual.getPhenotype() == null) {
            individual.setFitness(Double.MAX_VALUE);
            return;
        }

        int incorrect = 0;
        for (int i = 0; i < 64; i++) {
            double a1 = (i & 32) == 0 ? 0.0 : 1.0;
            double a0 = (i & 16) == 0 ? 0.0 : 1.0;
            double d3 = (i & 8) == 0 ? 0.0 : 1.0;
            double d2 = (i & 4) == 0 ? 0.0 : 1.0;
            double d1 = (i & 2) == 0 ? 0.0 : 1.0;
            double d0 = (i & 1) == 0 ? 0.0 : 1.0;
            
            double[] inputs = {a1, a0, d3, d2, d1, d0};
            double expectedOutput = (a1 == 0.0 && a0 == 0.0) ? d0 :
                    (a1 == 0.0 && a0 == 1.0) ? d1 :
                            (a1 == 1.0 && a0 == 0.0) ? d2 : d3;
            
            double[] actual = individual.getPhenotype().execute(inputs);
            double actualOutput = (actual[0] >= 0.5) ? 1.0 : 0.0;
            
            if (Math.abs(actualOutput - expectedOutput) > 0.01) {
                incorrect++;
            }
        }
        individual.setFitness(incorrect);
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

