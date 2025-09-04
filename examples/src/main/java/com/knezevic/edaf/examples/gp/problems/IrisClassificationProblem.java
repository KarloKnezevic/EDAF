package com.knezevic.edaf.examples.gp.problems;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.tree.TreeIndividual;

import java.util.Map;

/**
 * A classification problem using a simplified Iris dataset.
 * The goal is to evolve a function that correctly classifies Iris flowers
 * into one of three species based on four measurements.
 *
 * Inputs: sepal_length, sepal_width, petal_length, petal_width
 * Output: 0 (setosa), 1 (versicolor), 2 (virginica)
 */
public class IrisClassificationProblem extends AbstractProblem<TreeIndividual> {

    public IrisClassificationProblem(Map<String, Object> params) {
        super(params);
    }

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
    public void evaluate(TreeIndividual individual) {
        int misclassified = 0;
        for (double[] dataPoint : DATA) {
            Map<String, Double> terminals = Map.of(
                "sepal_length", dataPoint[0],
                "sepal_width", dataPoint[1],
                "petal_length", dataPoint[2],
                "petal_width", dataPoint[3]
            );

            double expectedClass = dataPoint[4];
            double rawOutput = individual.getGenotype().evaluate(terminals);

            // Clamp and round the output to get a class prediction
            long predictedClass = Math.round(Math.max(0, Math.min(2, rawOutput)));

            if (predictedClass != (long) expectedClass) {
                misclassified++;
            }
        }
        individual.setFitness(misclassified);
    }
}
