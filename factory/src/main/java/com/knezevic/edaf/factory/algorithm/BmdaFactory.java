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
@SuppressWarnings({"rawtypes", "unchecked"})
public class BmdaFactory<T extends Individual> implements AlgorithmFactory {
    @Override
    public Algorithm<?> createAlgorithm(Configuration config, Problem<?> problem, Population<?> population,
                                     Selection<?> selection, Statistics<?> statistics,
                                     TerminationCondition<?> terminationCondition, Random random) throws Exception {
        int selectionSize = config.getAlgorithm().getSelection().getSize();
        return new Bmda<>((Problem) problem, (Population) population, (Selection) selection, (Statistics) statistics, (TerminationCondition) terminationCondition, selectionSize);
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
