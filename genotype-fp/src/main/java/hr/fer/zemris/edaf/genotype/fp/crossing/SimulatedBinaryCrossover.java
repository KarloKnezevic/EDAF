package hr.fer.zemris.edaf.genotype.fp.crossing;

import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.genotype.fp.FpIndividual;

import java.util.Random;

/**
 * Simulated Binary Crossover (SBX) for floating-point individuals.
 */
public class SimulatedBinaryCrossover implements Crossover<FpIndividual> {

    private final Random random;
    private final double distributionIndex;

    public SimulatedBinaryCrossover(Random random, double distributionIndex) {
        this.random = random;
        this.distributionIndex = distributionIndex;
    }

    @Override
    public FpIndividual crossover(FpIndividual parent1, FpIndividual parent2) {
        double[] p1 = parent1.getGenotype();
        double[] p2 = parent2.getGenotype();
        int length = p1.length;
        double[] offspring = new double[length];

        for (int i = 0; i < length; i++) {
            double u = random.nextDouble();
            double beta;
            if (u <= 0.5) {
                beta = Math.pow(2 * u, 1.0 / (distributionIndex + 1.0));
            } else {
                beta = Math.pow(1.0 / (2.0 * (1.0 - u)), 1.0 / (distributionIndex + 1.0));
            }

            double c1 = 0.5 * ((1 + beta) * p1[i] + (1 - beta) * p2[i]);
            double c2 = 0.5 * ((1 - beta) * p1[i] + (1 + beta) * p2[i]);

            // For this simple implementation, we create one offspring.
            // A more complete implementation would create two.
            offspring[i] = random.nextBoolean() ? c1 : c2;
        }

        return new FpIndividual(offspring);
    }
}
