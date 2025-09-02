package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.genotype.binary.crossing.NPointCrossover;
import com.knezevic.edaf.genotype.binary.crossing.OnePointCrossover;
import com.knezevic.edaf.genotype.binary.crossing.UniformCrossover;
import com.knezevic.edaf.genotype.binary.mutation.SimpleMutation;
import com.knezevic.edaf.genotype.fp.crossing.DiscreteRecombination;
import com.knezevic.edaf.genotype.fp.crossing.SimpleArithmeticRecombination;
import com.knezevic.edaf.genotype.fp.crossing.SimulatedBinaryCrossover;
import com.knezevic.edaf.genotype.fp.crossing.WholeArithmeticRecombination;
import com.knezevic.edaf.genotype.fp.mutation.PolynomialMutation;
import com.knezevic.edaf.genotype.integer.crossing.TwoPointCrossover;
import com.knezevic.edaf.genotype.integer.mutation.SimpleIntegerMutation;
import com.knezevic.edaf.genotype.permutation.crossing.CycleCrossover;
import com.knezevic.edaf.genotype.permutation.crossing.OrderCrossover;
import com.knezevic.edaf.genotype.permutation.crossing.PartiallyMappedCrossover;
import com.knezevic.edaf.genotype.permutation.mutation.*;

import java.util.Random;

public abstract class GeneticAlgorithmFactory implements AlgorithmFactory {
    @Override
    public Crossover createCrossover(Configuration config, Random random) {
        final var genotypeConfig = config.getProblem().getGenotype();
        var crossoverConfig = genotypeConfig.getCrossing();
        String crossoverName = crossoverConfig.getName();
        String genotypeType = genotypeConfig.getType();

        return switch (genotypeType) {
            case "binary" -> switch (crossoverName) {
                case "one-point" -> new OnePointCrossover(random);
                case "n-point" -> new NPointCrossover(random, crossoverConfig.getN());
                case "uniform" -> new UniformCrossover(random);
                default -> throw new IllegalArgumentException("Unknown binary crossover: " + crossoverName);
            };
            case "integer" -> switch (crossoverName) {
                case "one-point" -> new com.knezevic.edaf.genotype.integer.crossing.OnePointCrossover(random);
                case "two-point" -> new TwoPointCrossover(random);
                default -> throw new IllegalArgumentException("Unknown integer crossover: " + crossoverName);
            };
            case "fp" -> switch (crossoverName) {
                case "sbx" -> new SimulatedBinaryCrossover(random, crossoverConfig.getDistributionIndex());
                case "discrete" -> new DiscreteRecombination(random);
                case "simple-arithmetic" -> new SimpleArithmeticRecombination(random, crossoverConfig.getProbability());
                case "whole-arithmetic" -> new WholeArithmeticRecombination(crossoverConfig.getProbability());
                default -> throw new IllegalArgumentException("Unknown fp crossover: " + crossoverName);
            };
            case "permutation" -> switch (crossoverName) {
                case "pmx" -> new PartiallyMappedCrossover(random);
                case "ox" -> new OrderCrossover(random);
                case "cx" -> new CycleCrossover();
                default -> throw new IllegalArgumentException("Unknown permutation crossover: " + crossoverName);
            };
            default -> throw new IllegalArgumentException("Unknown genotype type: " + genotypeType);
        };
    }

    @Override
    public Mutation createMutation(Configuration config, Random random) {
        final var genotypeConfig = config.getProblem().getGenotype();
        var mutationConfig = genotypeConfig.getMutation();
        String mutationName = mutationConfig.getName();
        String genotypeType = genotypeConfig.getType();
        double mutationProbability = mutationConfig.getProbability();

        return switch (genotypeType) {
            case "binary" -> switch (mutationName) {
                case "simple" -> new SimpleMutation(random, mutationProbability);
                default -> throw new IllegalArgumentException("Unknown binary mutation: " + mutationName);
            };
            case "integer" -> switch (mutationName) {
                case "simple" -> new SimpleIntegerMutation(random, mutationProbability,
                        genotypeConfig.getMinBound(),
                        genotypeConfig.getMaxBound());
                default -> throw new IllegalArgumentException("Unknown integer mutation: " + mutationName);
            };
            case "fp" -> switch (mutationName) {
                case "polynomial" -> new PolynomialMutation(random, mutationProbability,
                        mutationConfig.getDistributionIndex(),
                        genotypeConfig.getLowerBound(),
                        genotypeConfig.getUpperBound());
                default -> throw new IllegalArgumentException("Unknown fp mutation: " + mutationName);
            };
            case "permutation" -> switch (mutationName) {
                case "swap" -> new SwapMutation(random);
                case "insert" -> new InsertMutation(random);
                case "inversion" -> new InversionMutation(random);
                case "scramble" -> new ScrambleMutation(random);
                case "shift" -> new ShiftMutation(random);
                default -> throw new IllegalArgumentException("Unknown permutation mutation: " + mutationName);
            };
            default -> throw new IllegalArgumentException("Unknown genotype type: " + genotypeType);
        };
    }
}
