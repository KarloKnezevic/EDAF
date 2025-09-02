package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.mimic.MIMIC;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;

import java.util.Random;

/**
 * A factory for creating {@link MIMIC} algorithm objects.
 */
public class MimicFactory implements AlgorithmFactory {
    @Override
    public Algorithm createAlgorithm(Configuration config, Problem problem, Population population,
                                     Selection selection, Statistics statistics,
                                     TerminationCondition terminationCondition, Random random) throws Exception {
        return new MIMIC(problem, population, selection, statistics, terminationCondition,
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
