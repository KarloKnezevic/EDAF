package com.knezevic.edaf.algorithm.cga;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;

import java.util.Random;

public class CgaProvider implements AlgorithmProvider {
    @Override
    public String id() {
        return "cga";
    }

    @Override
    public boolean supports(Class<?> genotypeType, Class<?> problemType) {
        return true;
    }

    @Override
    public Algorithm<?> create(Problem<?> problem,
                               Population<?> population,
                               Selection<?> selection,
                               Crossover<?> crossover,
                               Mutation<?> mutation,
                               Statistics<?> statistics,
                               TerminationCondition<?> terminationCondition,
                               Random random,
                               int virtualPop,
                               int genotypeLength) {
        if (genotypeLength <= 0) {
            throw new IllegalArgumentException("cGA requires genotype length; ensure binary genotype length is provided in config.");
        }
        return new cGA((Problem) problem, (TerminationCondition) terminationCondition, Math.max(2, virtualPop), genotypeLength, random);
    }
}


