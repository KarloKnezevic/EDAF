package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.ltga.LTGA;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;

import java.util.Random;

import com.knezevic.edaf.core.api.Individual;

public class BmdaFactory<T extends Individual> implements AlgorithmFactory<T> {
    @Override
    public Algorithm<T> createAlgorithm(Configuration config, Problem<T> problem, Population<T> population,
                                     Selection<T> selection, Statistics<T> statistics,
                                     TerminationCondition<T> terminationCondition, Random random) throws Exception {
        //todo: add parameters to config
        return null;
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
