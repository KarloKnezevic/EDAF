package com.knezevic.edaf.core.api;

import java.util.Collection;

/**
 * Represents a population of individuals.
 *
 * @param <T> The type of individual in the population.
 */
public interface Population<T extends Individual> extends Iterable<T> {

    /**
     * Gets the number of individuals in the population.
     * @return The size of the population.
     */
    int getSize();

    /**
     * Gets the individual at the specified index.
     * @param index The index of the individual.
     * @return The individual at the specified index.
     */
    T getIndividual(int index);

    /**
     * Adds an individual to the population.
     * @param individual The individual to add.
     */
    void add(T individual);

    /**
     * Adds all individuals from a collection to the population.
     * @param individuals The collection of individuals to add.
     */
    void addAll(Collection<T> individuals);

    /**
     * Replaces the individual at the specified index with a new one.
     * @param index The index of the individual to replace.
     * @param individual The new individual.
     */
    void setIndividual(int index, T individual);

    /**
     * Removes an individual from the population.
     * @param individual The individual to remove.
     */
    void remove(T individual);

    /**
     * Removes all individuals from the population.
     */
    void clear();

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
