package com.knezevic.edaf.core.api;

/**
 * The base interface for all optimization algorithms.
 *
 * @param <T> The type of individual in the population.
 */
public interface Algorithm<T extends Individual> {

    /**
     * Runs the optimization algorithm.
     */
    void run();

    /**
     * Gets the best individual found by the algorithm.
     *
     * @return The best individual.
     */
    T getBest();

    /**
     * Gets the current generation number.
     *
     * @return The current generation.
     */
    int getGeneration();

    /**
     * Gets the current population.
     *
     * @return The current population.
     */
    Population<T> getPopulation();

    /**
     * Sets the progress listener for this algorithm.
     *
     * @param listener The progress listener.
     */
    void setProgressListener(ProgressListener listener);
}
