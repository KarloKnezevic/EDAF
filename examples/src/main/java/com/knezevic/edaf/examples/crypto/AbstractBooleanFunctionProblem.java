package com.knezevic.edaf.examples.crypto;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Problem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An abstract base class for boolean function optimization problems.
 * It handles the common logic of parsing criteria and calculating fitness.
 *
 * @param <T> The type of individual to be evaluated.
 */
public abstract class AbstractBooleanFunctionProblem<T extends Individual> implements Problem<T> {

    protected final List<FitnessCriterion> criteria;
    protected final int n;

    public AbstractBooleanFunctionProblem(Map<String, Object> params) {
        if (params == null || !params.containsKey("n")) {
            throw new IllegalArgumentException("Parameter 'n' (number of variables) must be provided.");
        }
        this.n = (int) params.get("n");

        if (!params.containsKey("criteria")) {
            throw new IllegalArgumentException("Parameter 'criteria' must be provided.");
        }

        List<String> criteriaNames = (List<String>) params.get("criteria");
        this.criteria = new ArrayList<>();
        for (String name : criteriaNames) {
            switch (name.toLowerCase()) {
                case "balancedness":
                    this.criteria.add(new Balancedness(n));
                    break;
                case "nonlinearity":
                    this.criteria.add(new Nonlinearity(n));
                    break;
                case "algebraicdegree":
                    this.criteria.add(new AlgebraicDegree(n));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown criterion: " + name);
            }
        }
    }

    /**
     * Calculates the total fitness of a boolean function based on the configured criteria.
     *
     * @param truthTable The truth table of the boolean function.
     * @return The total fitness value.
     */
    protected double calculateFitness(int[] truthTable) {
        double totalFitness = 0;
        for (FitnessCriterion criterion : criteria) {
            totalFitness += criterion.compute(truthTable);
        }
        return totalFitness;
    }
}
