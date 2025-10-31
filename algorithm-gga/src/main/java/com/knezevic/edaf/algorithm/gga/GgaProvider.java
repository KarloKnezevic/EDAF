package com.knezevic.edaf.algorithm.gga;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;

import java.util.Random;

public class GgaProvider implements AlgorithmProvider {
    @Override
    public String id() {
        return "gga";
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
                            int elitism,
                            int genotypeLength) {
        if (population == null) throw new IllegalArgumentException("gGA requires a population.");
        if (selection == null) throw new IllegalArgumentException("gGA requires a selection method.");
        if (crossover == null) throw new IllegalArgumentException("gGA requires a crossover operator.");
        if (mutation == null) throw new IllegalArgumentException("gGA requires a mutation operator.");
        return new gGA(problem, population, selection, crossover, mutation, terminationCondition, elitism);
    }
}


