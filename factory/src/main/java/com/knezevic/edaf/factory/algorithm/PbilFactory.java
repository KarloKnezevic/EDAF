package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.pbil.Pbil;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;

import java.util.Random;

public class PbilFactory implements AlgorithmFactory {
    @Override
    public Algorithm createAlgorithm(Configuration config, Problem problem, Population population,
                                     Selection selection, Statistics statistics,
                                     TerminationCondition terminationCondition, Random random) throws Exception {
        return new Pbil(problem, statistics, terminationCondition,
                config.getAlgorithm().getPopulation().getSize(),
                (Double) config.getAlgorithm().getParameters().get("learningRate"));
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
