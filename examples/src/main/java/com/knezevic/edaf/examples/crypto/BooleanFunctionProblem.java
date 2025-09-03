package com.knezevic.edaf.examples.crypto;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.List;
import java.util.Map;

/**
 * A problem that defines the optimization of boolean functions based on a set of cryptographic criteria.
 */
public class BooleanFunctionProblem implements Problem<BinaryIndividual> {

    private final List<FitnessCriterion> criteria;
    private final int n;

    public BooleanFunctionProblem(Map<String, Object> params) {
        if (params == null || !params.containsKey("n")) {
            throw new IllegalArgumentException("Parameter 'n' (number of variables) must be provided.");
        }
        this.n = (int) params.get("n");

        if (!params.containsKey("criteria")) {
            throw new IllegalArgumentException("Parameter 'criteria' must be provided.");
        }

        List<String> criteriaNames = (List<String>) params.get("criteria");
        this.criteria = new java.util.ArrayList<>();
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


    @Override
    public void evaluate(BinaryIndividual individual) {
        byte[] genotype = individual.getGenotype();
        int[] function = new int[genotype.length];
        for (int i = 0; i < genotype.length; i++) {
            function[i] = genotype[i];
        }

        double totalFitness = 0;
        for (FitnessCriterion criterion : criteria) {
            totalFitness += criterion.compute(function);
        }

        individual.setFitness(totalFitness);
    }
}
