package com.knezevic.edaf.algorithm.fda;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.Random;

/**
 * Service Provider for the Factorized Distribution Algorithm (FDA).
 */
public class FdaProvider implements AlgorithmProvider {

    @Override
    public String id() {
        return "fda";
    }

    @Override
    public boolean supports(Class<?> genotypeType, Class<?> problemType) {
        // FDA supports binary genotypes
        // genotypeType is Genotype.class from factory, so we accept it
        // The actual type will be checked at runtime via Statistics component
        // and problem/individual types
        return true;
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
        
        // FDA requires Statistics component
        if (statistics == null) {
            throw new IllegalArgumentException("FDA requires a Statistics component");
        }
        
        // FDA requires Population
        if (population == null) {
            throw new IllegalArgumentException("FDA requires a Population component");
        }
        
        // FDA requires Selection
        if (selection == null) {
            throw new IllegalArgumentException("FDA requires a Selection component");
        }
        
        return new FDA(problem, population, selection, statistics, 
                      terminationCondition, selectionSize);
    }
}

