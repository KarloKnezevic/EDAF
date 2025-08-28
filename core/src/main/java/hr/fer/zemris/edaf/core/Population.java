package hr.fer.zemris.edaf.core;

import java.util.List;

/**
 * Represents a population of individuals.
 *
 * @param <T> The type of individual in the population.
 */
public interface Population<T extends Individual> extends List<T> {

    /**
     * Gets the best individual in the population.
     *
     * @return The best individual.
     */
    T getBest();

    /**
     * Gets the worst individual in the population.
     *
     * @return The worst individual.
     */
    T getWorst();

    /**
     * Sorts the population by fitness in ascending order (best first).
     */
    void sort();
}
