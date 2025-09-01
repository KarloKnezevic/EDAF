package com.knezevic.edaf.genotype.binary.mutation;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.Random;

/**
 * Simple mutation for binary individuals.
 * It flips each bit with a given probability.
 */
public class SimpleMutation implements Mutation<BinaryIndividual> {

    private final Random random;
    private final double mutationProbability;

    public SimpleMutation(Random random, double mutationProbability) {
        this.random = random;
        this.mutationProbability = mutationProbability;
    }

    @Override
    public void mutate(BinaryIndividual individual) {
        byte[] genotype = individual.getGenotype();
        for (int i = 0; i < genotype.length; i++) {
            if (random.nextDouble() < mutationProbability) {
                genotype[i] = (byte) (1 - genotype[i]);
            }
        }
    }
}
