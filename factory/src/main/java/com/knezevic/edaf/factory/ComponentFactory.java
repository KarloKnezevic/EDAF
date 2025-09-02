package com.knezevic.edaf.factory;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import java.util.Random;

/**
 * A factory for creating components of the framework.
 */
public interface ComponentFactory {

    /**
     * Creates a {@link Problem} instance.
     *
     * @param config The configuration.
     * @return A {@link Problem} instance.
     * @throws Exception If an error occurs while creating the problem.
     */
    Problem createProblem(Configuration config) throws Exception;

    /**
     * Creates a {@link Genotype} instance.
     *
     * @param config The configuration.
     * @param random The random number generator.
     * @return A {@link Genotype} instance.
     * @throws Exception If an error occurs while creating the genotype.
     */
    Genotype createGenotype(Configuration config, Random random) throws Exception;

    /**
     * Creates a {@link Population} instance.
     *
     * @param config The configuration.
     * @param genotype The genotype.
     * @return A {@link Population} instance.
     * @throws Exception If an error occurs while creating the population.
     */
    Population createPopulation(Configuration config, Genotype genotype) throws Exception;

    /**
     * Creates a {@link Statistics} instance.
     *
     * @param config The configuration.
     * @param genotype The genotype.
     * @param random The random number generator.
     * @return A {@link Statistics} instance.
     * @throws Exception If an error occurs while creating the statistics.
     */
    Statistics createStatistics(Configuration config, Genotype genotype, Random random) throws Exception;

    /**
     * Creates a {@link Selection} instance.
     *
     * @param config The configuration.
     * @param random The random number generator.
     * @return A {@link Selection} instance.
     * @throws Exception If an error occurs while creating the selection operator.
     */
    Selection createSelection(Configuration config, Random random) throws Exception;

    /**
     * Creates a {@link TerminationCondition} instance.
     *
     * @param config The configuration.
     * @return A {@link TerminationCondition} instance.
     * @throws Exception If an error occurs while creating the termination condition.
     */
    TerminationCondition createTerminationCondition(Configuration config) throws Exception;

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
    Algorithm createAlgorithm(Configuration config, Problem problem, Population population,
                              Selection selection, Statistics statistics,
                              TerminationCondition terminationCondition, Random random) throws Exception;
}
