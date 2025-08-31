package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;

import java.util.Random;

public interface AlgorithmFactory {
    Algorithm createAlgorithm(Configuration config, Problem problem, Population population,
                              Selection selection, Statistics statistics,
                              TerminationCondition terminationCondition, Random random) throws Exception;

    Crossover createCrossover(Configuration config, Random random);

    Mutation createMutation(Configuration config, Random random);
}
