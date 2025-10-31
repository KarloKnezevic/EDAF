package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.algorithm.cgp.operator.CgpCrossoverOperator;
import com.knezevic.edaf.algorithm.cgp.operator.CgpMutationOperator;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.runtime.RandomSource;
import com.knezevic.edaf.core.runtime.SplittableRandomSource;
import com.knezevic.edaf.core.spi.AlgorithmProvider;
import com.knezevic.edaf.genotype.tree.primitives.Function;

import java.util.List;
import java.util.Random;

/**
 * Service Provider for Cartesian Genetic Programming (CGP).
 */
public class CgpProvider implements AlgorithmProvider {

    @Override
    public String id() {
        return "cgp";
    }

    @Override
    public boolean supports(Class<?> genotypeType, Class<?> problemType) {
        // CGP requires problems that implement CgpProblem interface
        return CgpProblem.class.isAssignableFrom(problemType);
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
        
        // CGP requires CgpProblem
        if (!(problem instanceof CgpProblem cgpProblem)) {
            throw new IllegalArgumentException("CGP requires a problem that implements CgpProblem interface");
        }
        
        // CGP requires selection
        if (selection == null) {
            throw new IllegalArgumentException("CGP requires a selection method");
        }
        
        // CGP requires population for initialization
        if (population == null) {
            throw new IllegalArgumentException("CGP requires a population for initialization");
        }
        
        // Extract CGP-specific configuration from problem parameters or use defaults
        // For now, use defaults - in the future this could come from configuration
        CgpConfig config = new CgpConfig();
        config.setPopulationSize(population.getSize());
        config.setMutationRate(0.02); // Default mutation rate
        config.setRows(1); // Default rows
        config.setCols(20); // Default columns
        config.setLevelsBack(5); // Default levels back
        config.setUseCrossover(false); // Default: no crossover
        config.setCrossoverRate(0.8); // Default crossover rate if enabled
        config.setReplacementStrategy(ReplacementStrategy.GENERATIONAL); // Default: generational
        
        // Get function set from CgpProblem
        List<Function> functionSet = cgpProblem.getFunctionSet();
        if (functionSet == null || functionSet.isEmpty()) {
            throw new IllegalArgumentException(
                "CGP requires a non-empty function set. " +
                "CgpProblem implementations must provide the function set via getFunctionSet()."
            );
        }
        
        // Create RandomSource from Random
        // Convert java.util.Random to RandomSource for CGP components
        // Use a seed extracted from Random to create SplittableRandomSource
        RandomSource randomSource = new SplittableRandomSource(random.nextLong());
        
        // Create CGP components using RandomSource
        CgpDecoder decoder = new CgpDecoder(config, functionSet, 
                cgpProblem.getNumInputs(), cgpProblem.getNumOutputs());
        CgpGenotypeFactory genotypeFactory = new CgpGenotypeFactory(config, functionSet,
                cgpProblem.getNumInputs(), cgpProblem.getNumOutputs(), randomSource);
        CgpMutationOperator cgpMutation = new CgpMutationOperator(config, functionSet,
                cgpProblem.getNumInputs(), cgpProblem.getNumOutputs(), randomSource);
        CgpCrossoverOperator cgpCrossover = new CgpCrossoverOperator(randomSource);
        
        // Create and return CGP algorithm
        return new CgpAlgorithm(config, cgpProblem, decoder, genotypeFactory,
                (Selection) selection, cgpMutation, cgpCrossover, random,
                (TerminationCondition) terminationCondition);
    }
}

