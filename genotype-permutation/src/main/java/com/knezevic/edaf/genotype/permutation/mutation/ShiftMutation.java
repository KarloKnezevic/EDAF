package com.knezevic.edaf.genotype.permutation.mutation;

import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

import java.util.Random;

/**
 * Shift mutation.
 */
public class ShiftMutation implements Mutation<PermutationIndividual> {

    private final Random random;

    public ShiftMutation(Random random) {
        this.random = random;
    }

    @Override
    public void mutate(PermutationIndividual individual) {
        int[] genotype = individual.getGenotype();
        int length = genotype.length;
        int shift = random.nextInt(length - 1) + 1;
        int[] temp = new int[length];
        System.arraycopy(genotype, 0, temp, shift, length - shift);
        System.arraycopy(genotype, length - shift, temp, 0, shift);
        System.arraycopy(temp, 0, genotype, 0, length);
    }
}
