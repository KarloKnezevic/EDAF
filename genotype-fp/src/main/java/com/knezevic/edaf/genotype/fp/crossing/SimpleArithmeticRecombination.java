package com.knezevic.edaf.genotype.fp.crossing;

import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Random;

/**
 * Simple arithmetic recombination.
 * <p>
 * Creates a new individual by taking a weighted average of the two parents' genes.
 * The crossover is applied from a random point to the end of the genotype.
 */
public class SimpleArithmeticRecombination implements Crossover<FpIndividual> {

    private final Random random;
    private final double alpha;

    public SimpleArithmeticRecombination(Random random, double alpha) {
        this.random = random;
        this.alpha = alpha;
    }

    @Override
    public FpIndividual crossover(FpIndividual parent1, FpIndividual parent2) {
        double[] genotype1 = parent1.getGenotype();
        double[] genotype2 = parent2.getGenotype();
        int length = genotype1.length;
        double[] offspring = new double[length];

        int crossoverPoint = random.nextInt(length);

        for (int i = 0; i < crossoverPoint; i++) {
            offspring[i] = genotype1[i];
        }

        for (int i = crossoverPoint; i < length; i++) {
            offspring[i] = alpha * genotype1[i] + (1 - alpha) * genotype2[i];
        }

        return new FpIndividual(offspring);
    }
}
