package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.bmda.Bmda;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import java.util.Random;

/**
 * A factory for creating {@link Bmda} algorithm objects.
 *
 * @param <T> The type of individual.
 */
public class BmdaFactory<T extends Individual> implements AlgorithmFactory<T> {
    @Override
    public Algorithm<T> createAlgorithm(Configuration config, Problem<T> problem, Population<T> population,
                                     Selection<T> selection, Statistics<T> statistics,
                                     TerminationCondition<T> terminationCondition, Random random) throws Exception {
        int selectionSize = config.getAlgorithm().getSelection().getSize();
        return new Bmda<>(problem, population, selection, statistics, terminationCondition, selectionSize);
    }

    @Override
    public Crossover createCrossover(Configuration config, Random random) {
        return null;
    }

    @Override
    public Mutation createMutation(Configuration config, Random random) {
        return null;
    }
}
