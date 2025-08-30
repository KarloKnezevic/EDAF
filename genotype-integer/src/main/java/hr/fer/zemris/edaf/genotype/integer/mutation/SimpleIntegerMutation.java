package hr.fer.zemris.edaf.genotype.integer.mutation;

import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.genotype.integer.IntegerIndividual;

import java.util.Random;

/**
 * Simple mutation for integer individuals.
 * It changes each integer with a given probability to a new random value within the bounds.
 */
public class SimpleIntegerMutation implements Mutation<IntegerIndividual> {

    private final Random random;
    private final double mutationProbability;
    private final int min;
    private final int max;

    public SimpleIntegerMutation(Random random, double mutationProbability, int min, int max) {
        this.random = random;
        this.mutationProbability = mutationProbability;
        this.min = min;
        this.max = max;
    }

    @Override
    public void mutate(IntegerIndividual individual) {
        int[] genotype = individual.getGenotype();
        for (int i = 0; i < genotype.length; i++) {
            if (random.nextDouble() < mutationProbability) {
                genotype[i] = random.nextInt(max - min + 1) + min;
            }
        }
    }
}
