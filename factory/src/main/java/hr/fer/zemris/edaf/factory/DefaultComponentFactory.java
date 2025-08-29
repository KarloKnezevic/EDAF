package hr.fer.zemris.edaf.factory;

import hr.fer.zemris.edaf.algorithm.ega.eGA;
import hr.fer.zemris.edaf.algorithm.gga.gGA;
import hr.fer.zemris.edaf.algorithm.pbil.Pbil;
import hr.fer.zemris.edaf.algorithm.umda.Umda;
import hr.fer.zemris.edaf.configuration.pojos.Configuration;
import hr.fer.zemris.edaf.core.*;
import hr.fer.zemris.edaf.genotype.binary.BinaryGenotype;
import hr.fer.zemris.edaf.genotype.binary.BinaryIndividual;
import hr.fer.zemris.edaf.genotype.binary.crossing.OnePointCrossover;
import hr.fer.zemris.edaf.genotype.binary.crossing.UniformCrossover;
import hr.fer.zemris.edaf.genotype.binary.mutation.SimpleMutation;
import hr.fer.zemris.edaf.genotype.fp.FpGenotype;
import hr.fer.zemris.edaf.genotype.fp.crossing.SimulatedBinaryCrossover;
import hr.fer.zemris.edaf.genotype.fp.mutation.PolynomialMutation;
import hr.fer.zemris.edaf.genotype.integer.IntegerGenotype;
import hr.fer.zemris.edaf.genotype.integer.IntegerIndividual;
import hr.fer.zemris.edaf.genotype.integer.crossing.TwoPointCrossover;
import hr.fer.zemris.edaf.genotype.integer.mutation.SimpleIntegerMutation;
import hr.fer.zemris.edaf.algorithm.mimic.MIMIC;
import hr.fer.zemris.edaf.genotype.fp.FpIndividual;
import hr.fer.zemris.edaf.statistics.mimic.MimicStatistics;
import hr.fer.zemris.edaf.statistics.umda.UmdaBinaryStatistics;
import hr.fer.zemris.edaf.statistics.umda.UmdaFpStatistics;

import java.lang.reflect.Constructor;
import java.util.Random;

/**
 * The default implementation of the ComponentFactory.
 */
public class DefaultComponentFactory implements ComponentFactory {

    @Override
    public Problem createProblem(Configuration config) throws Exception {
        Class<?> problemClass = Class.forName(config.getProblem().getClassName());
        Constructor<?> problemConstructor = problemClass.getConstructor();
        return (Problem) problemConstructor.newInstance();
    }

    @Override
    public Genotype createGenotype(Configuration config, Random random) throws Exception {
        String type = config.getProblem().getGenotype().getType();
        if ("binary".equals(type)) {
            return new BinaryGenotype(config.getProblem().getGenotype().getLength(), random);
        } else if ("fp".equals(type)) {
            return new FpGenotype(config.getProblem().getGenotype().getLength(),
                    config.getProblem().getGenotype().getLowerBound(),
                    config.getProblem().getGenotype().getUpperBound(), random);
        } else if ("integer".equals(type)) {
            return new IntegerGenotype(config.getProblem().getGenotype().getLength(),
                    config.getProblem().getGenotype().getMinBound(),
                    config.getProblem().getGenotype().getMaxBound(), random);
        }
        return null;
    }

    @Override
    public Population createPopulation(Configuration config, Genotype genotype) throws Exception {
        Population population = new SimplePopulation();
        for (int i = 0; i < config.getAlgorithm().getPopulation().getSize(); i++) {
            if (genotype instanceof BinaryGenotype) {
                population.add(new BinaryIndividual((byte[]) genotype.create()));
            } else if (genotype instanceof FpGenotype) {
                population.add(new FpIndividual((double[]) genotype.create()));
            } else if (genotype instanceof IntegerGenotype) {
                population.add(new IntegerIndividual((int[]) genotype.create()));
            }
        }
        return population;
    }

    @Override
    public Statistics createStatistics(Configuration config, Genotype genotype, Random random) throws Exception {
        String algorithmName = config.getAlgorithm().getName();
        if ("umda".equals(algorithmName)) {
            if (genotype instanceof BinaryGenotype) {
                return new UmdaBinaryStatistics(genotype, random);
            } else if (genotype instanceof FpGenotype) {
                return new UmdaFpStatistics(genotype, random);
            }
        } else if ("mimic".equals(algorithmName)) {
            if (genotype instanceof BinaryGenotype) {
                return new MimicStatistics(genotype, random);
            }
        }
        return null;
    }

    @Override
    public Selection createSelection(Configuration config, Random random) throws Exception {
        String selectionName = config.getAlgorithm().getSelection().getName();
        if ("tournament".equals(selectionName)) {
            return new TournamentSelection(random, config.getAlgorithm().getSelection().getSize());
        } else if ("rouletteWheel".equals(selectionName)) {
            return new RouletteWheelSelection(random);
        }
        return null;
    }

    @Override
    public TerminationCondition createTerminationCondition(Configuration config) throws Exception {
        if (config.getAlgorithm().getTermination().getMaxGenerations() > 0) {
            return new MaxGenerations(config.getAlgorithm().getTermination().getMaxGenerations());
        }
        return null;
    }

    @Override
    public Algorithm createAlgorithm(Configuration config, Problem problem, Population population,
                                     Selection selection, Statistics statistics,
                                     TerminationCondition terminationCondition, Random random) throws Exception {
        String algorithmName = config.getAlgorithm().getName();
        if ("umda".equals(algorithmName)) {
            if (population == null) throw new IllegalArgumentException("UMDA requires a population.");
            return new Umda(problem, population, selection, statistics, terminationCondition,
                    config.getAlgorithm().getSelection().getSize());
        } else if ("pbil".equals(algorithmName)) {
            if (population == null) throw new IllegalArgumentException("PBIL requires a population.");
            return new Pbil(problem, statistics, terminationCondition,
                    config.getAlgorithm().getPopulation().getSize(),
                    (Double) config.getAlgorithm().getParameters().get("learningRate"));
        } else if ("gga".equals(algorithmName)) {
            if (population == null) throw new IllegalArgumentException("gGA requires a population.");
            if (selection == null) throw new IllegalArgumentException("gGA requires a selection method.");
            Crossover crossover = createCrossover(config, random);
            Mutation mutation = createMutation(config, random);
            return new gGA(problem, population, selection, crossover, mutation, terminationCondition,
                    config.getAlgorithm().getElitism());
        } else if ("ega".equals(algorithmName)) {
            if (population == null) throw new IllegalArgumentException("eGA requires a population.");
            if (selection == null) throw new IllegalArgumentException("eGA requires a selection method.");
            Crossover crossover = createCrossover(config, random);
            Mutation mutation = createMutation(config, random);
            return new eGA(problem, population, selection, crossover, mutation, terminationCondition);
        } else if ("cga".equals(algorithmName)) {
            return new hr.fer.zemris.edaf.algorithm.cga.cGA(problem, terminationCondition,
                    (Integer) config.getAlgorithm().getParameters().get("n"),
                    config.getProblem().getGenotype().getLength(), random);
        } else if ("mimic".equals(algorithmName)) {
            if (population == null) throw new IllegalArgumentException("MIMIC requires a population.");
            return new MIMIC(problem, population, selection, statistics, terminationCondition,
                    config.getAlgorithm().getSelection().getSize());
        }
        return null;
    }

    private Crossover createCrossover(Configuration config, Random random) {
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
                return new hr.fer.zemris.edaf.genotype.integer.crossing.OnePointCrossover(random);
            } else if ("twoPoint".equals(crossoverName)) {
                return new TwoPointCrossover(random);
            }
        } else if ("fp".equals(genotypeType)) {
            if ("sbx".equals(crossoverName)) {
                return new SimulatedBinaryCrossover(random, config.getProblem().getGenotype().getCrossing().getDistributionIndex());
            }
        }
        return null;
    }

    private Mutation createMutation(Configuration config, Random random) {
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
        }
        return null;
    }
}
