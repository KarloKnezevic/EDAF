package com.knezevic.edaf.algorithm.cgp.operator;

import com.knezevic.edaf.algorithm.cgp.CgpIndividual;
import com.knezevic.edaf.core.api.Crossover;

import java.util.Random;

/**
 * Performs one-point crossover on two CGP individuals.
 */
public class CgpCrossoverOperator implements Crossover<CgpIndividual> {

    private final Random random;

    public CgpCrossoverOperator(Random random) {
        this.random = random;
    }

    @Override
    public CgpIndividual crossover(CgpIndividual parent1, CgpIndividual parent2) {
        int[] genotype1 = parent1.getGenotype();
        int[] genotype2 = parent2.getGenotype();
        int length = genotype1.length;

        int[] offspringGenotype = new int[length];
        int crossoverPoint = random.nextInt(length);

        System.arraycopy(genotype1, 0, offspringGenotype, 0, crossoverPoint);
        System.arraycopy(genotype2, crossoverPoint, offspringGenotype, crossoverPoint, length - crossoverPoint);

        return new CgpIndividual(offspringGenotype);
    }
}
