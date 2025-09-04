package com.knezevic.edaf.examples.crypto;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

import java.util.Map;

/**
 * A problem that defines the optimization of boolean functions using a permutation encoding.
 * This encoding ensures that the generated functions are always balanced.
 */
public class BooleanFunctionPermutationProblem extends AbstractBooleanFunctionProblem<PermutationIndividual> {

    private final int truthTableSize;
    private final int numOnes;

    public BooleanFunctionPermutationProblem(Map<String, Object> params) {
        super(params);
        this.truthTableSize = 1 << n;
        this.numOnes = this.truthTableSize / 2;
    }

    @Override
    public void evaluate(PermutationIndividual individual) {
        int[] genotype = individual.getGenotype();
        int[] truthTable = new int[truthTableSize];

        // The first half of the permutation defines the positions of the '1's.
        for (int i = 0; i < numOnes; i++) {
            truthTable[genotype[i]] = 1;
        }

        double totalFitness = calculateFitness(truthTable);
        individual.setFitness(totalFitness);
    }
}
