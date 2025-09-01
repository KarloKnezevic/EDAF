package com.knezevic.edaf.genotype.integer;

import com.knezevic.edaf.core.api.Mutation;

import java.util.Random;

public class IntUniformMutation implements Mutation<IntegerIndividual> {

    private final double mutationRate;
    private final int lowerBound;
    private final int upperBound;
    private final Random random;

    public IntUniformMutation(double mutationRate, int lowerBound, int upperBound, Random random) {
        this.mutationRate = mutationRate;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.random = random;
    }

    @Override
    public void mutate(IntegerIndividual individual) {
        int[] genotype = individual.getGenotype();
        for (int i = 0; i < genotype.length; i++) {
            if (random.nextDouble() < mutationRate) {
                genotype[i] = random.nextInt(upperBound - lowerBound + 1) + lowerBound;
            }
        }
    }
}
