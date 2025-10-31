package com.knezevic.edaf.algorithm.umda;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;

import java.util.Random;

public class UmdaProvider implements AlgorithmProvider {

    @Override
    public String id() {
        return "umda";
    }

    @Override
    public boolean supports(Class<?> genotypeType, Class<?> problemType) {
        return true; // UMDA is generic over genotype/problem types in this implementation
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Algorithm<?> create(
                            Problem<?> problem,
                            Population<?> population,
                            Selection<?> selection,
                            Crossover<?> crossover,
                            Mutation<?> mutation,
                            Statistics<?> statistics,
                            TerminationCondition<?> terminationCondition,
                            Random random,
                            int selectionSize,
                            int genotypeLength) {
        return new Umda(problem, population, selection, statistics, terminationCondition, selectionSize);
    }
}


