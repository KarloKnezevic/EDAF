package com.knezevic.edaf.genotype.integer.crossing;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.genotype.integer.IntegerIndividual;

import java.util.Random;

/**
 * Two-point crossover for integer individuals.
 */
public class TwoPointCrossover implements Crossover<IntegerIndividual> {

    private final Random random;

    public TwoPointCrossover(Random random) {
        this.random = random;
    }

    @Override
    public IntegerIndividual crossover(IntegerIndividual parent1, IntegerIndividual parent2) {
        int[] p1 = parent1.getGenotype();
        int[] p2 = parent2.getGenotype();
        int length = p1.length;
        int[] offspring = new int[length];

        int point1 = random.nextInt(length);
        int point2 = random.nextInt(length);

        if (point1 > point2) {
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }

        for (int i = 0; i < length; i++) {
            if (i < point1 || i > point2) {
                offspring[i] = p1[i];
            } else {
                offspring[i] = p2[i];
            }
        }

        return new IntegerIndividual(offspring);
    }
}
