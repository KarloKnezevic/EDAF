package com.knezevic.edaf.algorithm.boa;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;

import java.util.Random;

public class BoaProvider implements AlgorithmProvider {
    @Override
    public String id() { return "boa"; }

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
        // Defaults can be overridden by future config mapping if extended
        int nInit = 10;
        int nIter = terminationCondition != null ? Integer.MAX_VALUE : 100; // iterate until TC or fallback 100
        int length = Math.max(1, genotypeLength);
        double min = -1.0;
        double max = 1.0;
        return new Boa((Problem) problem, nInit, nIter, length, min, max);
    }
}


