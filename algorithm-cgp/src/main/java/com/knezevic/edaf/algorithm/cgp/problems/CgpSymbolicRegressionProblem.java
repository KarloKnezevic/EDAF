package com.knezevic.edaf.algorithm.cgp.problems;

import com.knezevic.edaf.algorithm.cgp.CgpIndividual;
import com.knezevic.edaf.algorithm.cgp.CgpProblem;
import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.genotype.tree.primitives.Function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Symbolic regression problem for CGP.
 * Tries to find a function that fits the data points: y = x^4 + x^3 + x^2 + x
 */
public class CgpSymbolicRegressionProblem implements CgpProblem {
    private final Map<Double, Double> trainingData;
    private final List<Function> functionSet;

    public CgpSymbolicRegressionProblem(Map<String, Object> params) {
        // Generate training data for y = x^4 + x^3 + x^2 + x
        this.trainingData = new HashMap<>();
        for (double x = -5.0; x <= 5.0; x += 0.5) {
            double y = Math.pow(x, 4) + Math.pow(x, 3) + Math.pow(x, 2) + x;
            trainingData.put(x, y);
        }
        
        // Define function set
        this.functionSet = List.of(
            new Function("ADD", 2, args -> args[0] + args[1]),
            new Function("SUB", 2, args -> args[0] - args[1]),
            new Function("MUL", 2, args -> args[0] * args[1]),
            new Function("DIV", 2, args -> {
                if (Math.abs(args[1]) < 1e-6) return 1.0;
                return args[0] / args[1];
            }),
            new Function("POW", 2, args -> Math.pow(args[0], args[1]))
        );
    }

    @Override
    public int getNumInputs() {
        return 1; // x
    }

    @Override
    public int getNumOutputs() {
        return 1; // y
    }

    @Override
    public void evaluate(CgpIndividual individual) {
        if (individual.getPhenotype() == null) {
            individual.setFitness(Double.MAX_VALUE);
            return;
        }

        double totalError = 0.0;
        for (Map.Entry<Double, Double> entry : trainingData.entrySet()) {
            double[] inputs = {entry.getKey()};
            double expected = entry.getValue();
            double[] actual = individual.getPhenotype().execute(inputs);
            if (Double.isNaN(actual[0]) || Double.isInfinite(actual[0])) {
                totalError += 1e9;
            } else {
                totalError += Math.pow(expected - actual[0], 2);
            }
        }
        individual.setFitness(totalError / trainingData.size()); // MSE
    }

    @Override
    public OptimizationType getOptimizationType() {
        return OptimizationType.min;
    }

    @Override
    public List<Function> getFunctionSet() {
        return functionSet;
    }
}
