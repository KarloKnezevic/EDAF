package com.knezevic.edaf.genotype.fp.crossing;

import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Random;

/**
 * Discrete recombination.
 * <p>
 * For each gene, the value is chosen from one of the parents with equal probability.
 */
public class DiscreteRecombination implements Crossover<FpIndividual> {

    private final Random random;

    public DiscreteRecombination(Random random) {
        this.random = random;
    }

    @Override
    public FpIndividual crossover(FpIndividual parent1, FpIndividual parent2) {
        double[] genotype1 = parent1.getGenotype();
        double[] genotype2 = parent2.getGenotype();
        int length = genotype1.length;
        double[] offspring = new double[length];

        for (int i = 0; i < length; i++) {
            offspring[i] = random.nextBoolean() ? genotype1[i] : genotype2[i];
        }

        return new FpIndividual(offspring);
    }
}
