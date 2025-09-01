package com.knezevic.edaf.genotype.binary.crossing;

import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.Arrays;
import java.util.Random;

/**
 * N-point crossover.
 */
public class NPointCrossover implements Crossover<BinaryIndividual> {

    private final Random random;
    private final int n;

    public NPointCrossover(Random random, int n) {
        this.random = random;
        this.n = n;
    }

    @Override
    public BinaryIndividual crossover(BinaryIndividual parent1, BinaryIndividual parent2) {
        byte[] genotype1 = parent1.getGenotype();
        byte[] genotype2 = parent2.getGenotype();
        int length = genotype1.length;
        byte[] offspring = new byte[length];

        int[] points = new int[n];
        for (int i = 0; i < n; i++) {
            points[i] = random.nextInt(length);
        }
        Arrays.sort(points);

        boolean swap = false;
        int pointIndex = 0;
        for (int i = 0; i < length; i++) {
            if (pointIndex < n && i == points[pointIndex]) {
                swap = !swap;
                pointIndex++;
            }
            offspring[i] = swap ? genotype2[i] : genotype1[i];
        }

        return new BinaryIndividual(offspring);
    }
}
