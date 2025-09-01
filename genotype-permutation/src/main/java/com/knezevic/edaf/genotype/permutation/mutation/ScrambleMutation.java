package com.knezevic.edaf.genotype.permutation.mutation;

import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

import java.util.Random;

/**
 * Scramble mutation.
 */
public class ScrambleMutation implements Mutation<PermutationIndividual> {

    private final Random random;

    public ScrambleMutation(Random random) {
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
        for (int i = point2; i > point1; i--) {
            int j = random.nextInt(i - point1 + 1) + point1;
            int temp = genotype[i];
            genotype[i] = genotype[j];
            genotype[j] = temp;
        }
    }
}
