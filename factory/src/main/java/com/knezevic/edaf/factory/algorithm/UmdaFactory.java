package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.umda.Umda;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;

import java.util.Random;

import com.knezevic.edaf.core.api.Individual;

/**
 * A factory for creating {@link Umda} algorithm objects.
 *
 * @param <T> The type of individual.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class UmdaFactory implements AlgorithmFactory {
    @Override
    public Algorithm<?> createAlgorithm(Configuration config, Problem<?> problem, Population<?> population,
                                     Selection<?> selection, Statistics<?> statistics,
                                     TerminationCondition<?> terminationCondition, Random random) throws Exception {
        return new Umda((Problem) problem, (Population) population, (Selection) selection, (Statistics) statistics, (TerminationCondition) terminationCondition,
                config.getAlgorithm().getSelection().getSize());
    }

    @Override
    public Crossover<?> createCrossover(Configuration config, Random random) {
        return null;
    }

    @Override
    public Mutation<?> createMutation(Configuration config, Random random) {
        return null;
    }
}
