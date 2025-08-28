package hr.fer.zemris.edaf.examples;

import hr.fer.zemris.edaf.configuration.ConfigurationLoader;
import hr.fer.zemris.edaf.configuration.pojos.Configuration;
import hr.fer.zemris.edaf.core.*;
import hr.fer.zemris.edaf.factory.ComponentFactory;
import hr.fer.zemris.edaf.factory.DefaultComponentFactory;

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
        // 1. Create component factory
        ComponentFactory factory = new DefaultComponentFactory();
        Random random = new Random();

        // 2. Create components
        Problem problem = factory.createProblem(config);
        Genotype genotype = factory.createGenotype(config, random);
        Population population = factory.createPopulation(config, genotype);
        Statistics statistics = factory.createStatistics(config, genotype, random);
        Selection selection = factory.createSelection(config, random);
        TerminationCondition terminationCondition = factory.createTerminationCondition(config);
        Algorithm algorithm = factory.createAlgorithm(config, problem, population, selection, statistics, terminationCondition, random);

        // 3. Run algorithm
        if (algorithm != null) {
            algorithm.run();
            System.out.println("Best individual: " + algorithm.getBest());
            System.out.println("Fitness: " + algorithm.getBest().getFitness());
        }
    }
}
