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
        return BinaryIndividual.class.isAssignableFrom(genotypeType);
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
        
        return new FDA(problem, population, selection, statistics, 
                      terminationCondition, selectionSize);
    }
}

