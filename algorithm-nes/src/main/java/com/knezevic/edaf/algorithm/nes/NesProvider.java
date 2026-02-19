package com.knezevic.edaf.algorithm.nes;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Random;

/**
 * Service Provider for the Natural Evolution Strategies (NES) algorithm.
 * <p>
 * Default learning rates follow the SNES paper:
 * <ul>
 *     <li>eta_mu = 1.0</li>
 *     <li>eta_sigma = (3 + ln(d)) / (5 * sqrt(d)) where d is the genotype dimension</li>
 * </ul>
 * These can be overridden via algorithm parameters in the YAML config.
 * </p>
 */
public class NesProvider implements AlgorithmProvider {

    @Override
    public String id() {
        return "nes";
    }

    @Override
    public boolean supports(Class<?> genotypeType, Class<?> problemType) {
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

        int popSize = population != null ? population.getSize() : 50;

        // SNES default learning rates
        double etaMu = 1.0;
        double etaSigma = (3.0 + Math.log(genotypeLength)) / (5.0 * Math.sqrt(genotypeLength));

        NesStatistics nesStats = new NesStatistics(genotypeLength, random, etaMu, etaSigma);

        return new NES(
            (Problem<FpIndividual>) problem,
            (TerminationCondition<FpIndividual>) terminationCondition,
            nesStats,
            popSize
        );
    }
}
