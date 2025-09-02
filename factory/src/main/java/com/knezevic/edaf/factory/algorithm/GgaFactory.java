package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.gga.gGA;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
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

/**
 * A factory for creating {@link gGA} algorithm objects.
 */
public class GgaFactory implements AlgorithmFactory {
    @Override
    public Algorithm createAlgorithm(Configuration config, Problem problem, Population population, Selection selection, Statistics statistics, TerminationCondition terminationCondition, Random random) throws Exception {
        if (population == null) throw new IllegalArgumentException("gGA requires a population.");
        if (selection == null) throw new IllegalArgumentException("gGA requires a selection method.");
        Crossover crossover = createCrossover(config, random);
        Mutation mutation = createMutation(config, random);
        return new gGA(problem, population, selection, crossover, mutation, terminationCondition,
                config.getAlgorithm().getElitism());
    }

    @Override
    public Crossover createCrossover(Configuration config, Random random) {
        String crossoverName = config.getProblem().getGenotype().getCrossing().getName();
        String genotypeType = config.getProblem().getGenotype().getType();

        if ("binary".equals(genotypeType)) {
            if ("onePoint".equals(crossoverName)) {
                return new OnePointCrossover(random);
            } else if ("uniform".equals(crossoverName)) {
                return new UniformCrossover(random);
            }
        } else if ("integer".equals(genotypeType)) {
            if ("onePoint".equals(crossoverName)) {
                return new com.knezevic.edaf.genotype.integer.crossing.OnePointCrossover(random);
            } else if ("twoPoint".equals(crossoverName)) {
                return new TwoPointCrossover(random);
            }
        } else if ("fp".equals(genotypeType)) {
            if ("sbx".equals(crossoverName)) {
                return new SimulatedBinaryCrossover(random, config.getProblem().getGenotype().getCrossing().getDistributionIndex());
            } else if ("discrete".equals(crossoverName)) {
                return new DiscreteRecombination(random);
            } else if ("simpleArithmetic".equals(crossoverName)) {
                return new SimpleArithmeticRecombination(random);
            } else if ("wholeArithmetic".equals(crossoverName)) {
                return new WholeArithmeticRecombination(random);
            }
        } else if ("permutation".equals(genotypeType)) {
            if ("pmx".equals(crossoverName)) {
                return new PartiallyMappedCrossover(random);
            } else if ("ox".equals(crossoverName)) {
                return new OrderCrossover(random);
            } else if ("cx".equals(crossoverName)) {
                return new CycleCrossover(random);
            }
        }
        return null;
    }

    @Override
    public Mutation createMutation(Configuration config, Random random) {
        String mutationName = config.getProblem().getGenotype().getMutation().getName();
        String genotypeType = config.getProblem().getGenotype().getType();
        double mutationProbability = config.getProblem().getGenotype().getMutation().getProbability();

        if ("binary".equals(genotypeType)) {
            if ("simple".equals(mutationName)) {
                return new SimpleMutation(random, mutationProbability);
            }
        } else if ("integer".equals(genotypeType)) {
            if ("simple".equals(mutationName)) {
                return new SimpleIntegerMutation(random, mutationProbability,
                        config.getProblem().getGenotype().getMinBound(),
                        config.getProblem().getGenotype().getMaxBound());
            }
        } else if ("fp".equals(genotypeType)) {
            if ("polynomial".equals(mutationName)) {
                return new PolynomialMutation(random, mutationProbability,
                        config.getProblem().getGenotype().getMutation().getDistributionIndex(),
                        config.getProblem().getGenotype().getLowerBound(),
                        config.getProblem().getGenotype().getUpperBound());
            }
        } else if ("permutation".equals(genotypeType)) {
            if ("swap".equals(mutationName)) {
                return new SwapMutation(random, mutationProbability);
            } else if ("insert".equals(mutationName)) {
                return new InsertMutation(random, mutationProbability);
            } else if ("inversion".equals(mutationName)) {
                return new InversionMutation(random, mutationProbability);
            } else if ("scramble".equals(mutationName)) {
                return new ScrambleMutation(random, mutationProbability);
            } else if ("shift".equals(mutationName)) {
                return new ShiftMutation(random, mutationProbability);
            }
        }
        return null;
    }
}
