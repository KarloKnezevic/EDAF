package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.cga.cGA;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;

import java.util.Random;

/**
 * A factory for creating {@link cGA} algorithm objects.
 */
public class CgaFactory implements AlgorithmFactory {
    @Override
    public Algorithm createAlgorithm(Configuration config, Problem problem, Population population,
                                     Selection selection, Statistics statistics,
                                     TerminationCondition terminationCondition, Random random) throws Exception {
        return new cGA(problem, terminationCondition,
                (Integer) config.getAlgorithm().getParameters().get("n"),
                config.getProblem().getGenotype().getLength(), random);
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
