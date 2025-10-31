package com.knezevic.edaf.algorithm.mimic;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;

import java.util.Random;

public class MimicProvider implements AlgorithmProvider {
    @Override
    public String id() {
        return "mimic";
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
                               int selectionSize,
                               int genotypeLength) {
        if (population == null) throw new IllegalArgumentException("MIMIC requires a population.");
        if (selection == null) throw new IllegalArgumentException("MIMIC requires a selection method.");
        return new MIMIC((Problem) problem, (Population) population, (Selection) selection, (Statistics) statistics, (TerminationCondition) terminationCondition, selectionSize);
    }
}


