package com.knezevic.edaf.genotype.permutation.mutation;

import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

import java.util.Random;

/**
 * Swap mutation.
 */
public class SwapMutation implements Mutation<PermutationIndividual> {

    private final Random random;

    public SwapMutation(Random random) {
        this.random = random;
    }

    @Override
    public void mutate(PermutationIndividual individual) {
        int[] genotype = individual.getGenotype();
        int length = genotype.length;
        int point1 = random.nextInt(length);
        int point2 = random.nextInt(length);
        int temp = genotype[point1];
        genotype[point1] = genotype[point2];
        genotype[point2] = temp;
    }
}
