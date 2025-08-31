package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.boa.Boa;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;

import java.util.Random;

public class BoaFactory implements AlgorithmFactory {
    @Override
    public Algorithm createAlgorithm(Configuration config, Problem problem, Population population,
                                     Selection selection, Statistics statistics,
                                     TerminationCondition terminationCondition, Random random) throws Exception {
        int nInit = (int) config.getAlgorithm().getParameters().get("n_init");
        int nIter = (int) config.getAlgorithm().getParameters().get("n_iter");
        return new Boa(problem, nInit, nIter);
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
