package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;

import java.util.Random;

/**
 * A factory for creating {@link Algorithm} objects.
 *
 * @param <T> The type of individual.
 */
public interface AlgorithmFactory {
    /**
     * Creates an {@link Algorithm} instance.
     *
     * @param config The configuration.
     * @param problem The problem.
     * @param population The population.
     * @param selection The selection operator.
     * @param statistics The statistics.
     * @param terminationCondition The termination condition.
     * @param random The random number generator.
     * @return An {@link Algorithm} instance.
     * @throws Exception If an error occurs while creating the algorithm.
     */
    Algorithm<?> createAlgorithm(Configuration config, Problem<?> problem, Population<?> population,
                                 Selection<?> selection, Statistics<?> statistics,
                                 TerminationCondition<?> terminationCondition, Random random) throws Exception;

    /**
     * Creates a {@link Crossover} operator.
     *
     * @param config The configuration.
     * @param random The random number generator.
     * @return A {@link Crossover} operator.
     */
    Crossover<?> createCrossover(Configuration config, Random random);

    /**
     * Creates a {@link Mutation} operator.
     *
     * @param config The configuration.
     * @param random The random number generator.
     * @return A {@link Mutation} operator.
     */
    Mutation<?> createMutation(Configuration config, Random random);
}
