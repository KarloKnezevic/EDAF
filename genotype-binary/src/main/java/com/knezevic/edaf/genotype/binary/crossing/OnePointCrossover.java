package com.knezevic.edaf.genotype.binary.crossing;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.Random;

/**
 * One-point crossover for binary individuals.
 */
public class OnePointCrossover implements Crossover<BinaryIndividual> {

    private final Random random;

    public OnePointCrossover(Random random) {
        this.random = random;
    }

    @Override
    public BinaryIndividual crossover(BinaryIndividual parent1, BinaryIndividual parent2) {
        byte[] p1 = parent1.getGenotype();
        byte[] p2 = parent2.getGenotype();
        int length = p1.length;
        byte[] offspring = new byte[length];

        int crossoverPoint = random.nextInt(length);

        for (int i = 0; i < length; i++) {
            if (i < crossoverPoint) {
                offspring[i] = p1[i];
            } else {
                offspring[i] = p2[i];
            }
        }

        return new BinaryIndividual(offspring);
    }
}
