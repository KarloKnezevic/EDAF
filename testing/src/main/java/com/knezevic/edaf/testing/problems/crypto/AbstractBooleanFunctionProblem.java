package com.knezevic.edaf.testing.problems.crypto;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.impl.AbstractProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractBooleanFunctionProblem<T extends Individual> extends AbstractProblem<T> {

    protected final List<FitnessCriterion> criteria;
    protected final int n;

    public AbstractBooleanFunctionProblem(Map<String, Object> params) {
        super(params);
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

    protected double calculateFitness(int[] truthTable) {
        double totalFitness = 0;
        for (FitnessCriterion criterion : criteria) {
            totalFitness += criterion.compute(truthTable);
        }
        return totalFitness;
    }
}
