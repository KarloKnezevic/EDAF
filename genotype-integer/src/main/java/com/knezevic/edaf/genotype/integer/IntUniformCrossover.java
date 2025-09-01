package com.knezevic.edaf.genotype.integer;

import com.knezevic.edaf.core.api.Crossover;

import java.util.Random;

public class IntUniformCrossover implements Crossover<IntegerIndividual> {

    private final Random random;

    public IntUniformCrossover(Random random) {
        this.random = random;
    }

    @Override
    public IntegerIndividual crossover(IntegerIndividual parent1, IntegerIndividual parent2) {
        int[] genotype1 = parent1.getGenotype();
        int[] genotype2 = parent2.getGenotype();
        int length = genotype1.length;
        int[] offspringGenotype = new int[length];

        for (int i = 0; i < length; i++) {
            if (random.nextBoolean()) {
                offspringGenotype[i] = genotype1[i];
            } else {
                offspringGenotype[i] = genotype2[i];
            }
        }

        return new IntegerIndividual(offspringGenotype);
    }
}
