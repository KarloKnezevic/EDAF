package hr.fer.zemris.edaf.core;

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
}
