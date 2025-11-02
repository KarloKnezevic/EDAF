package com.knezevic.edaf.algorithm.cem;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;

import java.util.Random;

/**
 * Service Provider for the Cross-Entropy Method (CEM).
 */
public class CemProvider implements AlgorithmProvider {

    @Override
    public String id() {
        return "cem";
    }

    @Override
    public boolean supports(Class<?> genotypeType, Class<?> problemType) {
        // CEM supports both binary and floating-point genotypes
        // Note: genotypeType may be Genotype.class from factory, so we accept any genotype
        // The actual type will be checked at runtime via Statistics component
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
        
        // CEM requires Statistics component
        if (statistics == null) {
            throw new IllegalArgumentException("CEM requires a Statistics component");
        }
        
        // Default parameters if not specified in config
        int batchSize = selectionSize > 0 ? selectionSize : 100;
        double eliteFraction = 0.1; // Top 10% as elite
        double learningRate = 0.7; // Standard CEM learning rate
        
        return new CEM(problem, statistics, terminationCondition, 
                      batchSize, eliteFraction, learningRate);
    }
}

