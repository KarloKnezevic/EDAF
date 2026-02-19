package com.knezevic.edaf.examples;

import com.knezevic.edaf.configuration.ConfigurationLoader;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.runtime.*;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Command(name = "edaf", mixinStandardHelpOptions = true, version = "EDAF 2.0",
        description = "Estimation of Distribution Algorithms Framework.",
        subcommands = {GenerateConfigCommand.class})
public class Framework implements Callable<Integer>, ProgressListener {

    private static final Logger log = LoggerFactory.getLogger(Framework.class);
    private static final Logger resultLog = LoggerFactory.getLogger("edaf.results");

    @Parameters(index = "0", description = "The configuration file to run.", arity = "0..1")
    private String configFile;

    @Option(names = {"--seed"}, description = "Random seed for reproducible runs.")
    private Long seed;

    @Option(names = {"--metrics"}, description = "Enable Micrometer metrics (SimpleMeterRegistry).", defaultValue = "false")
    private boolean metrics;

    @Option(names = {"--prometheus-port"}, description = "Expose Prometheus scrape endpoint on given port (implies --metrics).")
    private Integer prometheusPort;

    @Option(names = {"--output-dir"}, description = "Directory for file-based result persistence.")
    private String outputDir;

    @Option(names = {"--output-format"}, description = "Output format: json or csv (default: json).", defaultValue = "json")
    private String outputFormat;

    @Option(names = {"--db-url"}, description = "JDBC URL for database persistence (e.g. jdbc:sqlite:edaf.db).")
    private String dbUrl;

    @Option(names = {"--db-user"}, description = "Database username.")
    private String dbUser;

    @Option(names = {"--db-password"}, description = "Database password.")
    private String dbPassword;

    @Option(names = {"--report-format"}, description = "Report format: html, md, or both.")
    private String reportFormat;

    @Option(names = {"--report-dir"}, description = "Output directory for reports (default: ./reports).", defaultValue = "./reports")
    private String reportDir;

    @Option(names = {"--dashboard-port"}, description = "Start real-time dashboard on given port.")
    private Integer dashboardPort;

    private ProgressBar progressBar;
    private int logFrequency = 10;

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

        if (prometheusPort != null) {
            System.setProperty("edaf.metrics.prometheus.port", String.valueOf(prometheusPort));
        }

        run(config);
        return 0;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void run(Configuration config) throws Exception {
        // Resolve seed from CLI, system property, or generate one
        long effectiveSeed = resolveSeed();
        Random random = new Random(effectiveSeed);
        log.info("Using random seed: {}", effectiveSeed);

        // Read log frequency from config (default 10)
        int configLogFreq = config.getAlgorithm().getLogFrequency();
        this.logFrequency = configLogFreq > 0 ? configLogFreq : 10;

        // 1. Create components
        Algorithm<?> algorithm = createComponents(config, random);
        if (algorithm == null) {
            log.error("Algorithm could not be created. Check configuration and algorithm provider registration.");
            return;
        }
        algorithm.setProgressListener(this);

        // 2. Build event publisher chain
        CompositeEventPublisher composite = buildEventPublisher(config);

        // 3. Wire execution context and run
        wireAndRun(algorithm, composite, config, effectiveSeed);
    }

    private long resolveSeed() {
        if (seed != null) {
            return seed;
        }
        String seedProp = System.getProperty("edaf.seed");
        if (seedProp != null) {
            return Long.parseLong(seedProp);
        }
        return System.nanoTime();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Algorithm<?> createComponents(Configuration config, Random random) throws Exception {
        ComponentFactory factory = new SpiBackedComponentFactory();

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
        return factory.createAlgorithm(config, problem, population, selection, statistics, terminationCondition, random);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private CompositeEventPublisher buildEventPublisher(Configuration config) {
        CompositeEventPublisher composite = new CompositeEventPublisher();

        // Metrics publishers
        if (metrics || prometheusPort != null) {
            System.setProperty("edaf.metrics.enabled", "true");
            if (prometheusPort != null) {
                addPublisherByClassName(composite, "com.knezevic.edaf.metrics.PrometheusEventPublisher");
            } else {
                addPublisherByClassName(composite, "com.knezevic.edaf.metrics.MicrometerEventPublisher");
            }
        }

        // File-based persistence
        String effectiveOutputDir = outputDir;
        String effectiveOutputFormat = outputFormat;
        if (effectiveOutputDir == null && config.getOutput() != null
                && config.getOutput().getPersistence() != null
                && config.getOutput().getPersistence().isEnabled()
                && "file".equals(config.getOutput().getPersistence().getType())) {
            var fileCfg = config.getOutput().getPersistence().getFile();
            if (fileCfg != null) {
                effectiveOutputDir = fileCfg.getDirectory();
                effectiveOutputFormat = fileCfg.getFormat();
            }
        }
        if (effectiveOutputDir != null) {
            var fileSink = new com.knezevic.edaf.persistence.file.FileResultSink(
                java.nio.file.Path.of(effectiveOutputDir), effectiveOutputFormat);
            composite.addPublisher(
                new com.knezevic.edaf.persistence.api.PersistenceEventPublisher(fileSink));
        }

        // JDBC persistence
        String effectiveDbUrl = dbUrl;
        if (effectiveDbUrl == null && config.getOutput() != null
                && config.getOutput().getPersistence() != null
                && config.getOutput().getPersistence().isEnabled()
                && "jdbc".equals(config.getOutput().getPersistence().getType())) {
            var jdbcCfg = config.getOutput().getPersistence().getJdbc();
            if (jdbcCfg != null) {
                effectiveDbUrl = jdbcCfg.getUrl();
                if (dbUser == null) dbUser = jdbcCfg.getUser();
                if (dbPassword == null) dbPassword = jdbcCfg.getPassword();
            }
        }
        if (effectiveDbUrl != null) {
            var ds = com.knezevic.edaf.persistence.jdbc.DataSourceFactory.create(
                effectiveDbUrl, dbUser, dbPassword);
            com.knezevic.edaf.persistence.jdbc.SchemaInitializer.initialize(ds);
            var jdbcSink = new com.knezevic.edaf.persistence.jdbc.JdbcResultSink(ds);
            composite.addPublisher(
                new com.knezevic.edaf.persistence.api.PersistenceEventPublisher(jdbcSink));
        }

        // Reporting
        String effectiveReportFormat = reportFormat;
        String effectiveReportDir = reportDir;
        if (effectiveReportFormat == null && config.getOutput() != null
                && config.getOutput().getReporting() != null
                && config.getOutput().getReporting().isEnabled()) {
            effectiveReportFormat = config.getOutput().getReporting().getFormat();
            effectiveReportDir = config.getOutput().getReporting().getOutputDirectory();
        }
        if (effectiveReportFormat != null) {
            java.nio.file.Path repDir = java.nio.file.Path.of(effectiveReportDir);
            if ("html".equalsIgnoreCase(effectiveReportFormat) || "both".equalsIgnoreCase(effectiveReportFormat)) {
                var htmlSink = new com.knezevic.edaf.reporting.ReportResultSink(
                    new com.knezevic.edaf.reporting.HtmlReportGenerator(), repDir);
                composite.addPublisher(
                    new com.knezevic.edaf.persistence.api.PersistenceEventPublisher(htmlSink));
            }
            if ("md".equalsIgnoreCase(effectiveReportFormat) || "both".equalsIgnoreCase(effectiveReportFormat)) {
                var mdSink = new com.knezevic.edaf.reporting.ReportResultSink(
                    new com.knezevic.edaf.reporting.MarkdownReportGenerator(), repDir);
                composite.addPublisher(
                    new com.knezevic.edaf.persistence.api.PersistenceEventPublisher(mdSink));
            }
        }

        return composite;
    }

    private void addPublisherByClassName(CompositeEventPublisher composite, String className) {
        try {
            Class<?> cls = Class.forName(className);
            composite.addPublisher((EventPublisher) cls.getConstructor().newInstance());
        } catch (Exception e) {
            Throwable cause = e.getCause();
            while (cause != null && !(cause instanceof java.net.BindException)) {
                cause = cause.getCause();
            }
            if (cause instanceof java.net.BindException) {
                log.error("Port is already in use. Stop existing EDAF process or use a different port.");
            } else {
                log.warn("Failed to create {}: {}", className, e.getMessage());
            }
        }
    }

    private void wireAndRun(Algorithm<?> algorithm, CompositeEventPublisher composite,
                            Configuration config, long effectiveSeed) {
        // Dashboard setup (tracked for cleanup)
        com.knezevic.edaf.dashboard.DashboardServer dashboardServer = null;
        Integer effectiveDashboardPort = dashboardPort;
        if (effectiveDashboardPort == null && config.getOutput() != null
                && config.getOutput().getDashboard() != null
                && config.getOutput().getDashboard().isEnabled()) {
            effectiveDashboardPort = config.getOutput().getDashboard().getPort();
        }
        if (effectiveDashboardPort != null) {
            var dashboardEvents = new com.knezevic.edaf.dashboard.DashboardEventPublisher();
            dashboardServer = new com.knezevic.edaf.dashboard.DashboardServer(
                effectiveDashboardPort, dashboardEvents);
            dashboardServer.start();
            composite.addPublisher(dashboardEvents);
        }

        RandomSource rs = new SplittableRandomSource(effectiveSeed);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        ExecutionContext ctx = new ExecutionContext(rs, executor, composite);

        if (algorithm instanceof SupportsExecutionContext s) {
            s.setExecutionContext(ctx);
        }

        try {
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
        } finally {
            executor.shutdownNow();
            if (dashboardServer != null) {
                dashboardServer.stop();
            }
        }
    }

    @Override
    public void onGenerationDone(int generation, Individual bestInGeneration, Population population) {
        if (progressBar != null) {
            progressBar.step();

            if (population != null && population.getSize() > 0) {
                PopulationStatistics.Statistics stats =
                    PopulationStatistics.calculate(population);

                if (bestInGeneration != null) {
                    progressBar.setExtraMessage(String.format("Gen %d | Best: %.4f | Avg: %.4f | Std: %.4f",
                        generation, stats.best(), stats.avg(), stats.std()));
                }

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
