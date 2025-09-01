package com.knezevic.edaf.genotype.binary.mutation;

import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.Random;

/**
 * Complete mixing mutation.
 * <p>
 * This mutation operator creates a new random genotype.
 */
public class CompleteMixingMutation implements Mutation<BinaryIndividual> {

    private final Random random;

    public CompleteMixingMutation(Random random) {
        this.random = random;
    }

    @Override
    public void mutate(BinaryIndividual individual) {
        byte[] genotype = individual.getGenotype();
        for (int i = 0; i < genotype.length; i++) {
            genotype[i] = (byte) random.nextInt(2);
        }
    }
}
