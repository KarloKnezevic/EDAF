package com.knezevic.edaf.algorithm.gp;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;

import java.util.Random;

public class GpProvider implements AlgorithmProvider {
    @Override
    public String id() { return "gp"; }

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
                               int genotypeLength,
                               int selectionSize) {
        if (population == null) throw new IllegalArgumentException("GP requires a population.");
        if (selection == null) throw new IllegalArgumentException("GP requires a selection method.");
        if (crossover == null) throw new IllegalArgumentException("GP requires a crossover operator.");
        if (mutation == null) throw new IllegalArgumentException("GP requires a mutation operator.");
        double crossoverRate = 0.9;
        double mutationRate = 0.1;
        int elitismSize = 0;
        return new GeneticProgrammingAlgorithm((Problem) problem, (Population) population, (Selection) selection, (Crossover) crossover, (Mutation) mutation, (TerminationCondition) terminationCondition, crossoverRate, mutationRate, elitismSize, random);
    }
}


