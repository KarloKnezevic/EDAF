package com.knezevic.edaf.genotype.fp.crossing;

import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.genotype.fp.FpIndividual;

/**
 * Whole arithmetic recombination.
 * <p>
 * Creates a new individual by taking a weighted average of the two parents' genes.
 * The crossover is applied to the entire genotype.
 */
public class WholeArithmeticRecombination implements Crossover<FpIndividual> {

    private final double alpha;

    public WholeArithmeticRecombination(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public FpIndividual crossover(FpIndividual parent1, FpIndividual parent2) {
        double[] genotype1 = parent1.getGenotype();
        double[] genotype2 = parent2.getGenotype();
        int length = genotype1.length;
        double[] offspring = new double[length];

        for (int i = 0; i < length; i++) {
            offspring[i] = alpha * genotype1[i] + (1 - alpha) * genotype2[i];
        }

        return new FpIndividual(offspring);
    }
}
