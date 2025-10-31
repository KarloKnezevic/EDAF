package com.knezevic.edaf.testing.problems.gp;

import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.tree.TreeIndividual;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SymbolicRegressionProblem extends AbstractProblem<TreeIndividual> {
    private final Map<Double, Double> trainingData;
    public SymbolicRegressionProblem(java.util.Map<String, Object> params) {
        super(params);
        this.trainingData = new ConcurrentHashMap<>();
        for (double i = -10.0; i <= 10.0; i += 1.0) {
            double x = i;
            double y = Math.pow(x, 4) + Math.pow(x, 3) + Math.pow(x, 2) + x;
            trainingData.put(x, y);
        }
    }

    @Override
    public void evaluate(TreeIndividual individual) {
        double totalError = 0.0;
        for (Map.Entry<Double, Double> entry : trainingData.entrySet()) {
            double x = entry.getKey();
            double expectedY = entry.getValue();
            Map<String, Double> terminalValues = Map.of("x", x);
            double actualY = individual.getGenotype().evaluate(terminalValues);
            if (Double.isInfinite(actualY) || Double.isNaN(actualY)) {
                totalError += 1e9;
            } else {
                totalError += Math.pow(expectedY - actualY, 2);
            }
        }
        double mse = totalError / trainingData.size();
        individual.setFitness(mse);
    }
}


