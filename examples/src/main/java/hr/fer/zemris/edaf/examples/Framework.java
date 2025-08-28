package hr.fer.zemris.edaf.examples;

import hr.fer.zemris.edaf.configuration.ConfigurationLoader;
import hr.fer.zemris.edaf.configuration.pojos.Configuration;
import hr.fer.zemris.edaf.core.*;
import hr.fer.zemris.edaf.factory.ComponentFactory;
import hr.fer.zemris.edaf.factory.DefaultComponentFactory;
import net.logstash.logback.marker.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Random;
import java.util.concurrent.Callable;

@Command(name = "edaf", mixinStandardHelpOptions = true, version = "EDAF 2.0",
        description = "Estimation of Distribution Algorithms Framework.",
        subcommands = {GenerateConfigCommand.class})
public class Framework implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(Framework.class);
    private static final Logger resultLog = LoggerFactory.getLogger("hr.fer.zemris.edaf.results");

    @Parameters(index = "0", description = "The configuration file to run.", arity = "0..1")
    private String configFile;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Framework()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        if (configFile == null) {
            log.info("No configuration file provided. Use 'edaf <config.yaml>' to run an experiment, or 'edaf --help' for more options.");
            return 0;
        }

        log.info("=================================================");
        log.info("   Estimation of Distribution Algorithms Framework (EDAF)   ");
        log.info("=================================================");

        log.info("PHASE 1: Loading configuration from '{}'", configFile);
        ConfigurationLoader loader = new ConfigurationLoader();
        Configuration config = loader.load(configFile);
        log.info("Configuration loaded successfully.");

        run(config);
        return 0;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void run(Configuration config) throws Exception {
        // 1. Create component factory
        log.info("PHASE 2: Initializing framework components...");
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
        log.info("Framework components initialized successfully.");

        // 3. Run algorithm
        log.info("PHASE 3: Starting algorithm '{}'...", config.getAlgorithm().getName());
        if (algorithm != null) {
            algorithm.run();
            log.info("Algorithm execution finished.");

            log.info("-------------------- RESULTS --------------------");
            Individual best = algorithm.getBest();
            log.info("Best individual: {}", best);
            log.info("Fitness: {}", best.getFitness());
            log.info("-----------------------------------------------");

            resultLog.info(Markers.append("best_individual", best), "Final result");
        } else {
            log.error("Algorithm could not be created. Exiting.");
        }

        log.info("=================================================");
        log.info("   Framework execution finished.                 ");
        log.info("=================================================");
    }
}
