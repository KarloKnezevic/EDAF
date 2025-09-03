package com.knezevic.edaf.examples.crypto;

/**
 * An interface for a fitness criterion used in boolean function optimization.
 */
public interface FitnessCriterion {
    /**
     * Computes the fitness of a given boolean function based on this criterion.
     *
     * @param function The boolean function, represented as an array of 0s and 1s.
     * @return The fitness value.
     */
    double compute(int[] function);
}
