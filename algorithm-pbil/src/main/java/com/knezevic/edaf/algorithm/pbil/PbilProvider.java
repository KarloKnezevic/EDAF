package com.knezevic.edaf.algorithm.pbil;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;

import java.util.Random;

public class PbilProvider implements AlgorithmProvider {
    @Override
    public String id() {
        return "pbil";
    }

    @Override
    public boolean supports(Class<?> genotypeType, Class<?> problemType) {
        return true;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Algorithm<?> create(Problem<?> problem,
                               Population<?> population,
                               Selection<?> selection,
                               Crossover<?> crossover,
                               Mutation<?> mutation,
                               Statistics<?> statistics,
                               TerminationCondition<?> terminationCondition,
                               Random random,
                               int ignored,
                               int genotypeLength) {
        int popSize = population != null ? population.getSize() : 100;
        double learningRate = 0.1;
        return new Pbil(problem, statistics, terminationCondition, popSize, learningRate);
    }
}


