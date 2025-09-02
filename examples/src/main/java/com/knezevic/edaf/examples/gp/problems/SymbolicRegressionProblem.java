package com.knezevic.edaf.examples.gp.problems;

import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.genotype.tree.TreeIndividual;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A symbolic regression problem for Genetic Programming.
 * The goal is to find a function that best fits a given target function.
 * Target function: y = x^4 + x^3 + x^2 + x
 */
public class SymbolicRegressionProblem implements Problem<TreeIndividual> {

    private final Map<Double, Double> trainingData;

    public SymbolicRegressionProblem() {
        this.trainingData = new ConcurrentHashMap<>();
        // Generate 20 data points from -10 to 10
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

            // The map of terminal values for evaluation
            Map<String, Double> terminalValues = Map.of("x", x);

            double actualY = individual.getGenotype().evaluate(terminalValues);

            // Handle cases where evaluation fails (e.g., division by zero)
            if (Double.isInfinite(actualY) || Double.isNaN(actualY)) {
                totalError += 1e9; // Assign a large penalty
            } else {
                totalError += Math.pow(expectedY - actualY, 2);
            }
        }

        // The fitness is the mean squared error
        double mse = totalError / trainingData.size();
        individual.setFitness(mse);
    }
}
