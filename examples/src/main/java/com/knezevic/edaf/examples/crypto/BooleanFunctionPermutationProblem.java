package com.knezevic.edaf.examples.crypto;

import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A problem that defines the optimization of boolean functions using a permutation encoding.
 * This encoding ensures that the generated functions are always balanced.
 */
public class BooleanFunctionPermutationProblem implements Problem<PermutationIndividual> {

    private final List<FitnessCriterion> criteria;
    private final int n;
    private final int truthTableSize;
    private final int numOnes;

    public BooleanFunctionPermutationProblem(Map<String, Object> params) {
        if (params == null || !params.containsKey("n")) {
            throw new IllegalArgumentException("Parameter 'n' (number of variables) must be provided.");
        }
        this.n = (int) params.get("n");
        this.truthTableSize = 1 << n;
        this.numOnes = this.truthTableSize / 2;

        if (!params.containsKey("criteria")) {
            throw new IllegalArgumentException("Parameter 'criteria' must be provided.");
        }

        List<String> criteriaNames = (List<String>) params.get("criteria");
        this.criteria = new ArrayList<>();
        for (String name : criteriaNames) {
            switch (name.toLowerCase()) {
                case "balancedness":
                    // This criterion is not very useful here, as all functions are balanced by definition.
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
    public void evaluate(PermutationIndividual individual) {
        int[] genotype = individual.getGenotype();
        int[] truthTable = new int[truthTableSize];

        // The first half of the permutation defines the positions of the '1's.
        for (int i = 0; i < numOnes; i++) {
            truthTable[genotype[i]] = 1;
        }

        double totalFitness = 0;
        for (FitnessCriterion criterion : criteria) {
            totalFitness += criterion.compute(truthTable);
        }

        individual.setFitness(totalFitness);
    }
}
