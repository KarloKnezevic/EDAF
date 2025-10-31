package com.knezevic.edaf.testing.problems.crypto;

import com.knezevic.edaf.genotype.permutation.PermutationIndividual;
import java.util.Map;

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
        for (int i = 0; i < numOnes; i++) {
            truthTable[genotype[i]] = 1;
        }
        double totalFitness = calculateFitness(truthTable);
        individual.setFitness(totalFitness);
    }
}


