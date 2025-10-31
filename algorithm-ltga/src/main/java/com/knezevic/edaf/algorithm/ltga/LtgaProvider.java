package com.knezevic.edaf.algorithm.ltga;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;

import java.util.Random;

public class LtgaProvider implements AlgorithmProvider {
    @Override
    public String id() {
        return "ltga";
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
        if (population == null) throw new IllegalArgumentException("LTGA requires a population.");
        if (selection == null) throw new IllegalArgumentException("LTGA requires a selection method.");
        if (mutation == null) throw new IllegalArgumentException("LTGA requires a mutation operator.");
        return new LTGA((Problem) problem, (Population) population, (Selection) selection, (Mutation) mutation, (TerminationCondition) terminationCondition, genotypeLength, random);
    }
}


