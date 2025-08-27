package hr.fer.zemris.edaf.examples;

import hr.fer.zemris.edaf.algorithm.umda.Umda;
import hr.fer.zemris.edaf.configuration.ConfigurationLoader;
import hr.fer.zemris.edaf.configuration.pojos.Configuration;
import hr.fer.zemris.edaf.core.Algorithm;
import hr.fer.zemris.edaf.core.Genotype;
import hr.fer.zemris.edaf.core.MaxGenerations;
import hr.fer.zemris.edaf.core.Population;
import hr.fer.zemris.edaf.core.Problem;
import hr.fer.zemris.edaf.core.Selection;
import hr.fer.zemris.edaf.core.SimplePopulation;
import hr.fer.zemris.edaf.core.Statistics;
import hr.fer.zemris.edaf.core.TerminationCondition;
import hr.fer.zemris.edaf.core.TournamentSelection;
import hr.fer.zemris.edaf.genotype.binary.BinaryGenotype;
import hr.fer.zemris.edaf.genotype.binary.BinaryIndividual;
import hr.fer.zemris.edaf.statistics.umda.UmdaBinaryStatistics;

import java.lang.reflect.Constructor;
import java.util.Random;

/**
 * The main class for running experiments.
 */
public class Framework {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java -jar edaf.jar <config.yaml>");
            return;
        }

        ConfigurationLoader loader = new ConfigurationLoader();
        Configuration config = loader.load(args[0]);

        run(config);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void run(Configuration config) throws Exception {
        // 1. Create components from configuration
        Random random = new Random();

        // Problem
        Class<?> problemClass = Class.forName(config.getProblem().getClassName());
        Constructor<?> problemConstructor = problemClass.getConstructor();
        Problem problem = (Problem) problemConstructor.newInstance();

        // Genotype
        Genotype genotype = null;
        if ("binary".equals(config.getProblem().getGenotype().getType())) {
            Class<?> genotypeClass = Class.forName("hr.fer.zemris.edaf.genotype.binary.BinaryGenotype");
            Constructor<?> genotypeConstructor = genotypeClass.getConstructor(int.class, Random.class);
            genotype = (Genotype) genotypeConstructor.newInstance(config.getProblem().getGenotype().getLength(), random);
        }

        // Population
        Population population = new SimplePopulation();
        for (int i = 0; i < config.getAlgorithm().getPopulation().getSize(); i++) {
            population.add(new BinaryIndividual((byte[]) genotype.create()));
        }

        // Statistics
        Statistics statistics = null;
        if ("umda".equals(config.getAlgorithm().getName()) && "binary".equals(config.getProblem().getGenotype().getType())) {
            Class<?> statisticsClass = Class.forName("hr.fer.zemris.edaf.statistics.umda.UmdaBinaryStatistics");
            Constructor<?> statisticsConstructor = statisticsClass.getConstructor(Genotype.class, Random.class);
            statistics = (Statistics) statisticsConstructor.newInstance(genotype, random);
        }

        // Selection
        Selection selection = null;
        if ("tournament".equals(config.getAlgorithm().getSelection().getName())) {
            selection = new TournamentSelection(random, config.getAlgorithm().getSelection().getSize());
        }

        // Termination condition
        TerminationCondition terminationCondition = null;
        if (config.getAlgorithm().getTermination().getMaxGenerations() > 0) {
            terminationCondition = new MaxGenerations(config.getAlgorithm().getTermination().getMaxGenerations());
        }

        // Algorithm
        Algorithm algorithm = null;
        if ("umda".equals(config.getAlgorithm().getName())) {
            algorithm = new Umda(problem, population, selection, statistics, terminationCondition, config.getAlgorithm().getSelection().getSize());
        }

        // 2. Run algorithm
        if (algorithm != null) {
            algorithm.run();
            System.out.println("Best individual: " + algorithm.getBest());
            System.out.println("Fitness: " + algorithm.getBest().getFitness());
        }
    }
}
