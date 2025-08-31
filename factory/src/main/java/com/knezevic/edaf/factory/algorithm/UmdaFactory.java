package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.umda.Umda;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;

import java.util.Random;

import com.knezevic.edaf.core.api.Individual;

public class UmdaFactory<T extends Individual> implements AlgorithmFactory<T> {
    @Override
    public Algorithm<T> createAlgorithm(Configuration config, Problem<T> problem, Population<T> population,
                                     Selection<T> selection, Statistics<T> statistics,
                                     TerminationCondition<T> terminationCondition, Random random) throws Exception {
        return new Umda<>(problem, population, selection, statistics, terminationCondition,
                config.getAlgorithm().getSelection().getSize());
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
