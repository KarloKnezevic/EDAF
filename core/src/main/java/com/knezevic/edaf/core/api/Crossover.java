package com.knezevic.edaf.core.api;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;

/**
 * Defines the crossover operator.
 *
 * @param <T> The type of individual.
 */
public interface Crossover<T extends Individual> {

    /**
     * Performs crossover on two parents to produce one offspring.
     *
     * @param parent1 The first parent.
     * @param parent2 The second parent.
     * @return The offspring.
     */
    T crossover(T parent1, T parent2);
}
