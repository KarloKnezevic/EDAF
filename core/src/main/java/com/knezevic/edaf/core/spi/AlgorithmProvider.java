package com.knezevic.edaf.core.spi;
import com.knezevic.edaf.core.api.Algorithm;
import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.api.Statistics;
import com.knezevic.edaf.core.api.TerminationCondition;

import java.util.Random;

/**
 * SPI for discovering algorithm implementations at runtime.
 * Implementations should be registered via META-INF/services.
 */
public interface AlgorithmProvider {

    /**
     * Unique identifier used in configuration (e.g. "umda", "gga", "ega").
     */
    String id();

    /**
     * Returns true if this algorithm supports the given genotype and problem types.
     */
    boolean supports(Class<?> genotypeType, Class<?> problemType);

    /**
     * Construct a new algorithm instance using provided components and configuration.
     */
    Algorithm<?> create(
                     Problem<?> problem,
                     Population<?> population,
                     Selection<?> selection,
                     Crossover<?> crossover,
                     Mutation<?> mutation,
                     Statistics<?> statistics,
                     TerminationCondition<?> terminationCondition,
                     Random random,
                     int selectionSize,
                     int genotypeLength);

    /**
     * Optional extended constructor that receives additional execution limits derived from configuration
     * without introducing a dependency on the configuration module.
     * Default implementation delegates to {@link #create(Problem, Population, Selection, Crossover, Mutation, Statistics, TerminationCondition, Random, int, int)}.
     *
     * @param problem           problem instance
     * @param population        initial population (may be null for algorithms that don't use it)
     * @param selection         selection operator (may be null if not applicable)
     * @param crossover         crossover operator (may be null if not applicable)
     * @param mutation          mutation operator (may be null if not applicable)
     * @param statistics        statistics/model component (may be null if not applicable)
     * @param terminationCondition termination condition
     * @param random            RNG
     * @param selectionSize     selection size derived from config/population
     * @param genotypeLength    genotype length derived from config
     * @param maxGenerations    maximum generations derived from config termination
     * @return algorithm instance
     */
    default Algorithm<?> createWithConfig(
            Problem<?> problem,
            Population<?> population,
            Selection<?> selection,
            Crossover<?> crossover,
            Mutation<?> mutation,
            Statistics<?> statistics,
            TerminationCondition<?> terminationCondition,
            Random random,
            int selectionSize,
            int genotypeLength,
            int maxGenerations) {
        return create(problem, population, selection, crossover, mutation, statistics,
                terminationCondition, random, selectionSize, genotypeLength);
    }
}


