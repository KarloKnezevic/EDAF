package com.knezevic.edaf.core.api;

/**
 * Defines the selection operator.
 *
 * @param <T> The type of individual in the population.
 */
public interface Selection<T extends Individual> {

    /**
     * Selects a new population from the given population.
     *
     * @param population The population to select from.
     * @param size The number of individuals to select.
     * @return A new population of selected individuals.
     */
    Population<T> select(Population<T> population, int size);
}
