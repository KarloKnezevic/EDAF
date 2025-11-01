package com.knezevic.edaf.examples;

import com.knezevic.edaf.configuration.ConfigurationLoader;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.factory.ComponentFactory;
import com.knezevic.edaf.factory.SpiBackedComponentFactory;
import net.logstash.logback.marker.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import java.util.Random;
import java.util.concurrent.Callable;
import com.knezevic.edaf.examples.StatisticsTableFormatter;
import com.knezevic.edaf.core.runtime.PopulationStatistics;

@Command(name = "edaf", mixinStandardHelpOptions = true, version = "EDAF 2.0",
        description = "Estimation of Distribution Algorithms Framework.",
        subcommands = {GenerateConfigCommand.class})
public class Framework implements Callable<Integer>, ProgressListener {

    private static final Logger log = LoggerFactory.getLogger(Framework.class);
    private static final Logger resultLog = LoggerFactory.getLogger("edaf.results");

    @Parameters(index = "0", description = "The configuration file to run.", arity = "0..1")
    private String configFile;

    private ProgressBar progressBar;

    @Option(names = {"--metrics"}, description = "Enable Micrometer metrics (SimpleMeterRegistry).", defaultValue = "false")
    private boolean metrics;

    @Option(names = {"--prometheus-port"}, description = "Expose Prometheus scrape endpoint on given port (implies --metrics).")
    private Integer prometheusPort;

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

        ConfigurationLoader loader = new ConfigurationLoader();
        Configuration config = loader.load(configFile);

        if (metrics || prometheusPort != null) {
            System.setProperty("edaf.metrics.enabled", "true");
            if (prometheusPort != null) {
                System.setProperty("edaf.metrics.prometheus.port", String.valueOf(prometheusPort));
            }
        }

        run(config);
        return 0;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void run(Configuration config) throws Exception {
        // 1. Create component factory
        ComponentFactory factory = new SpiBackedComponentFactory();
        Random random = new Random();

        // 2. Create components
        Problem<?> problem = factory.createProblem(config);
        Genotype<?> genotype = factory.createGenotype(config, random);
        Population<?> population = null;
        if (config.getAlgorithm().getPopulation() != null) {
            population = factory.createPopulation(config, genotype);
        }
        Statistics<?> statistics = factory.createStatistics(config, genotype, random);
        Selection<?> selection = null;
        if (config.getAlgorithm().getSelection() != null) {
            selection = factory.createSelection(config, random);
        }
        TerminationCondition<?> terminationCondition = factory.createTerminationCondition(config);
        Algorithm<?> algorithm = factory.createAlgorithm(config, problem, population, selection, statistics, terminationCondition, random);
        if (algorithm == null) {
            log.error("Algorithm could not be created. Check configuration and algorithm provider registration.");
            return;
        }
        algorithm.setProgressListener(this);
        
        // Create ExecutionContext with metrics if enabled
        if (metrics || prometheusPort != null) {
            com.knezevic.edaf.core.runtime.RandomSource rs = 
                new com.knezevic.edaf.core.runtime.SplittableRandomSource(System.currentTimeMillis());
            java.util.concurrent.ExecutorService executor = 
                java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();
            com.knezevic.edaf.core.runtime.EventPublisher events = 
                new com.knezevic.edaf.core.runtime.ConsoleEventPublisher();
            
            if (prometheusPort != null) {
                try {
                    Class<?> cls = Class.forName("com.knezevic.edaf.metrics.PrometheusEventPublisher");
                    events = (com.knezevic.edaf.core.runtime.EventPublisher) cls.getConstructor().newInstance();
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    while (cause != null && !(cause instanceof java.net.BindException)) {
                        cause = cause.getCause();
                    }
                    if (cause instanceof java.net.BindException) {
                        log.error("Port {} is already in use. Stop existing EDAF process or use different port.", prometheusPort);
                        log.warn("Continuing with ConsoleEventPublisher - metrics not available");
                    } else {
                        log.warn("Failed to create PrometheusEventPublisher: {}", e.getMessage());
                        log.warn("Continuing with ConsoleEventPublisher - metrics not available");
                    }
                }
            } else {
                try {
                    Class<?> cls = Class.forName("com.knezevic.edaf.metrics.MicrometerEventPublisher");
                    events = (com.knezevic.edaf.core.runtime.EventPublisher) cls.getConstructor().newInstance();
                } catch (Exception e) {
                    log.warn("Failed to create MicrometerEventPublisher: {}", e.getMessage());
                }
            }
            
            com.knezevic.edaf.core.runtime.ExecutionContext ctx = 
                new com.knezevic.edaf.core.runtime.ExecutionContext(rs, executor, events);
            
            if (algorithm instanceof com.knezevic.edaf.core.runtime.SupportsExecutionContext s) {
                s.setExecutionContext(ctx);
            }
        }
        
        // 3. Run algorithm
        if (algorithm != null) {
            ProgressBarBuilder pbb = new ProgressBarBuilder()
                    .setTaskName("Generations")
                    .setInitialMax(config.getAlgorithm().getTermination().getMaxGenerations())
                    .setStyle(ProgressBarStyle.ASCII)
                    .setUnit(" gen", 1)
                    .setUpdateIntervalMillis(100);

            try (ProgressBar pb = pbb.build()) {
                this.progressBar = pb;
                algorithm.run();
            }

            Individual<?> best = algorithm.getBest();
            System.out.println("\nBest fitness: " + best.getFitness());
            resultLog.info(Markers.append("best_individual", best), "Final result");
        } else {
            log.error("Algorithm could not be created. Exiting.");
        }
    }

    @Override
    public void onGenerationDone(int generation, Individual bestInGeneration, Population population) {
        if (progressBar != null) {
            progressBar.step();
            
            // Calculate detailed statistics
            if (population != null && population.getSize() > 0) {
                PopulationStatistics.Statistics stats = 
                    PopulationStatistics.calculate(population);
                
                // Update progress bar with compact info
                if (bestInGeneration != null) {
                    progressBar.setExtraMessage(String.format("Gen %d | Best: %.4f | Avg: %.4f | Std: %.4f", 
                        generation, stats.best(), stats.avg(), stats.std()));
                }
                
                // Print detailed statistics table every N generations or at important milestones
                int logFrequency = 10; // Log every 10 generations
                if (generation % logFrequency == 0 || generation == 1) {
                    String table = StatisticsTableFormatter.format(generation, stats);
                    System.out.print(table);
                }
            } else if (bestInGeneration != null) {
                progressBar.setExtraMessage(String.format("Gen %d | Best: %.4f", generation, bestInGeneration.getFitness()));
            }
        }
    }
}
