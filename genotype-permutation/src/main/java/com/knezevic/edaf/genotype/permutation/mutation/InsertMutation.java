package com.knezevic.edaf.genotype.permutation.mutation;

import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

import java.util.Random;

/**
 * Insert mutation.
 */
public class InsertMutation implements Mutation<PermutationIndividual> {

    private final Random random;

    public InsertMutation(Random random) {
        this.random = random;
    }

    @Override
    public void mutate(PermutationIndividual individual) {
        int[] genotype = individual.getGenotype();
        int length = genotype.length;
        int from = random.nextInt(length);
        int to = random.nextInt(length);
        int gene = genotype[from];
        if (from < to) {
            System.arraycopy(genotype, from + 1, genotype, from, to - from);
        } else {
            System.arraycopy(genotype, to, genotype, to + 1, from - to);
        }
        genotype[to] = gene;
    }
}
