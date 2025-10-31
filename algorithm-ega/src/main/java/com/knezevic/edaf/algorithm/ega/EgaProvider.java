package com.knezevic.edaf.algorithm.ega;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;

import java.util.Random;

public class EgaProvider implements AlgorithmProvider {
    @Override
    public String id() {
        return "ega";
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
        if (population == null) throw new IllegalArgumentException("eGA requires a population.");
        if (selection == null) throw new IllegalArgumentException("eGA requires a selection method.");
        if (crossover == null) throw new IllegalArgumentException("eGA requires a crossover operator.");
        if (mutation == null) throw new IllegalArgumentException("eGA requires a mutation operator.");
        return new eGA(problem, population, selection, crossover, mutation, terminationCondition);
    }
}


