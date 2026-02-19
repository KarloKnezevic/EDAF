package com.knezevic.edaf.algorithm.nes;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.genotype.fp.FpIndividual;

/**
 * Rank-based fitness shaping for Natural Evolution Strategies.
 * <p>
 * Transforms raw fitness values into utility values based on rank ordering.
 * This makes the algorithm invariant to monotone transformations of the fitness
 * function and reduces sensitivity to outliers.
 * </p>
 * <p>
 * The utility for rank k (0-indexed, best first) is:
 * <pre>
 *   u_k = max(0, log(lambda/2 + 1) - log(k + 1)) / Z - 1/lambda
 * </pre>
 * where Z is the normalizing constant ensuring utilities sum to 1 (before centering).
 * </p>
 *
 * @see <a href="https://arxiv.org/abs/1106.4487">Wierstra et al., "Natural Evolution Strategies"</a>
 */
public final class FitnessShaping {

    private FitnessShaping() {}

    /**
     * Computes rank-based utility values for each individual in the population.
     * The returned array is indexed by population position (not by rank).
     *
     * @param population the evaluated population
     * @param opt whether we are minimizing or maximizing
     * @return utility values aligned with population indices
     */
    public static double[] computeUtilities(Population<FpIndividual> population, OptimizationType opt) {
        int lambda = population.getSize();

        // Build indices sorted by fitness (best first)
        Integer[] indices = new Integer[lambda];
        for (int i = 0; i < lambda; i++) {
            indices[i] = i;
        }
        java.util.Arrays.sort(indices, (a, b) -> {
            double fa = population.getIndividual(a).getFitness();
            double fb = population.getIndividual(b).getFitness();
            return opt == OptimizationType.min
                ? Double.compare(fa, fb)
                : Double.compare(fb, fa);
        });

        // Compute raw utilities by rank
        double[] rawUtilities = new double[lambda];
        double sumU = 0;
        for (int k = 0; k < lambda; k++) {
            rawUtilities[k] = Math.max(0.0, Math.log(lambda / 2.0 + 1.0) - Math.log(k + 1.0));
            sumU += rawUtilities[k];
        }

        // Normalize and center, then map back to population order
        double[] result = new double[lambda];
        for (int k = 0; k < lambda; k++) {
            result[indices[k]] = rawUtilities[k] / sumU - 1.0 / lambda;
        }
        return result;
    }
}
