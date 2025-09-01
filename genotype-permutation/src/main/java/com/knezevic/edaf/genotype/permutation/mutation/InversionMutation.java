package com.knezevic.edaf.genotype.permutation.mutation;

import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

import java.util.Random;

/**
 * Inversion mutation.
 */
public class InversionMutation implements Mutation<PermutationIndividual> {

    private final Random random;

    public InversionMutation(Random random) {
        this.random = random;
    }

    @Override
    public void mutate(PermutationIndividual individual) {
        int[] genotype = individual.getGenotype();
        int length = genotype.length;
        int point1 = random.nextInt(length);
        int point2 = random.nextInt(length);
        if (point1 > point2) {
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }
        while (point1 < point2) {
            int temp = genotype[point1];
            genotype[point1] = genotype[point2];
            genotype[point2] = temp;
            point1++;
            point2--;
        }
    }
}
