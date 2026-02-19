package com.knezevic.edaf.genotype.realvalued.mutation;

import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.genotype.realvalued.RealValuedIndividual;

import java.util.Random;

/**
 * Self-adaptive mutation using the log-normal rule from Evolution Strategies.
 * Mutates sigmas first using log-normal distribution, then mutates variables using the new sigmas.
 * tau = 1 / sqrt(2*n), tau' = 1 / sqrt(2*sqrt(n))
 */
public class SelfAdaptiveMutation implements Mutation<RealValuedIndividual> {

    private final Random random;
    private final double lowerBound;
    private final double upperBound;
    private static final double SIGMA_MIN = 1e-10;

    public SelfAdaptiveMutation(Random random, double lowerBound, double upperBound) {
        this.random = random;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public void mutate(RealValuedIndividual individual) {
        double[] genotype = individual.getGenotype();
        double[] sigmas = individual.getSigmas();
        int n = genotype.length;

        double tau = 1.0 / Math.sqrt(2.0 * n);
        double tauPrime = 1.0 / Math.sqrt(2.0 * Math.sqrt(n));

        // Global factor applied to all sigmas
        double globalFactor = Math.exp(tauPrime * random.nextGaussian());

        for (int i = 0; i < n; i++) {
            // Mutate sigma
            sigmas[i] = sigmas[i] * globalFactor * Math.exp(tau * random.nextGaussian());
            sigmas[i] = Math.max(SIGMA_MIN, sigmas[i]);

            // Mutate variable using mutated sigma
            genotype[i] = genotype[i] + sigmas[i] * random.nextGaussian();

            // Clamp to bounds
            genotype[i] = Math.max(lowerBound, Math.min(upperBound, genotype[i]));
        }
    }
}
