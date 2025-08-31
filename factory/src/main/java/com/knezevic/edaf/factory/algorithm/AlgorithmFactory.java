package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;

import java.util.Random;

public interface AlgorithmFactory<T extends Individual> {
    Algorithm<T> createAlgorithm(Configuration config, Problem<T> problem, Population<T> population,
                                 Selection<T> selection, Statistics<T> statistics,
                                 TerminationCondition<T> terminationCondition, Random random) throws Exception;

    Crossover createCrossover(Configuration config, Random random);

    Mutation createMutation(Configuration config, Random random);
}
