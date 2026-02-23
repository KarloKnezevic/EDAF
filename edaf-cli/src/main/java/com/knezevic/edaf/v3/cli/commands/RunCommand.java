package com.knezevic.edaf.v3.cli.commands;

import com.knezevic.edaf.v3.cli.ui.ConsoleUiSink;
import com.knezevic.edaf.v3.cli.ui.Verbosity;
import com.knezevic.edaf.v3.cli.logging.LoggingConfigurator;
import com.knezevic.edaf.v3.core.config.ConfigLoader;
import com.knezevic.edaf.v3.core.config.ExperimentConfig;
import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.experiments.runner.ExperimentRunner;
import com.knezevic.edaf.v3.experiments.runner.RunExecution;
import com.knezevic.edaf.v3.persistence.jdbc.DataSourceFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.Callable;

/**
 * Runs one experiment config file.
 */
@Command(name = "run", description = "Run one experiment configuration")
public final class RunCommand implements Callable<Integer> {

    @Option(names = {"-c", "--config"}, required = true, description = "Path to config YAML")
    private Path configPath;

    @Option(names = "--verbosity", description = "Override verbosity: quiet|normal|verbose|debug")
    private String verbosity;

    @Override
    public Integer call() {
        Verbosity requestedVerbosity = verbosity != null && !verbosity.isBlank()
                ? Verbosity.from(verbosity)
                : Verbosity.NORMAL;
        LoggingConfigurator.apply(requestedVerbosity);

        ConfigLoader loader = new ConfigLoader();
        var loaded = loader.load(configPath);
        var config = loaded.config();

        if (verbosity != null && !verbosity.isBlank()) {
            config.getLogging().setVerbosity(verbosity);
        }

        Verbosity effectiveVerbosity = Verbosity.from(config.getLogging().getVerbosity());
        LoggingConfigurator.apply(effectiveVerbosity);

        ExperimentRunner runner = new ExperimentRunner();
        int runCount = Math.max(1, config.getRun().getRunCount());
        String baseRunId = config.getRun().getId();
        long baseSeed = config.getRun().getMasterSeed();
        String baseName = config.getRun().getName();

        for (String warning : loaded.warnings()) {
            System.out.println("[WARN] " + warning);
        }

        for (int index = 0; index < runCount; index++) {
            var perRunLoaded = index == 0 ? loaded : loader.load(configPath);
            var perRunConfig = perRunLoaded.config();
            perRunConfig.getRun().setRunCount(1);

            if (verbosity != null && !verbosity.isBlank()) {
                perRunConfig.getLogging().setVerbosity(verbosity);
            }

            if (runCount > 1) {
                perRunConfig.getRun().setId(baseRunId + "-r" + String.format(Locale.ROOT, "%02d", index + 1));
                perRunConfig.getRun().setMasterSeed(baseSeed + index);
                perRunConfig.getRun().setName(baseName + " [" + (index + 1) + "/" + runCount + "]");
            }

            AtomicBoolean runFinished = new AtomicBoolean(false);
            Thread shutdownHook = createShutdownStopHook(perRunConfig, runFinished);
            if (shutdownHook != null) {
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            }

            List<EventSink> extraSinks = createExtraSinks(perRunConfig, effectiveVerbosity);
            RunExecution execution;
            try {
                execution = runner.run(perRunConfig, extraSinks);
                runFinished.set(true);
            } finally {
                runFinished.set(true);
                removeShutdownHookQuietly(shutdownHook);
            }

            for (String warning : execution.warnings()) {
                System.out.println("[WARN] " + warning);
            }

            if (extraSinks.isEmpty()) {
                System.out.println("Run finished: best=" + execution.result().best().fitness().scalar()
                        + " iterations=" + execution.result().iterations()
                        + " evals=" + execution.result().evaluations());
                if (!execution.artifacts().isEmpty()) {
                    System.out.println("Artifacts: " + execution.artifacts());
                }
            }
        }

        if (runCount > 1) {
            System.out.println("Completed multi-run execution: runs=" + runCount);
        }

        return 0;
    }

    private static List<EventSink> createExtraSinks(com.knezevic.edaf.v3.core.config.ExperimentConfig config,
                                                    Verbosity verbosity) {
        List<EventSink> extraSinks = new ArrayList<>();
        if (config.getLogging().getModes().stream().map(String::toLowerCase).anyMatch("console"::equals)) {
            extraSinks.add(new ConsoleUiSink(
                    verbosity,
                    config.getStopping().getMaxIterations(),
                    config.getObservability().getMetricsEveryIterations()
            ));
        }
        return extraSinks;
    }

    private static Thread createShutdownStopHook(ExperimentConfig config, AtomicBoolean runFinished) {
        if (config == null || runFinished == null) {
            return null;
        }
        if (!isDbSinkEnabled(config)) {
            return null;
        }
        String runId = config.getRun() == null ? null : config.getRun().getId();
        String url = config.getPersistence() == null || config.getPersistence().getDatabase() == null
                ? null
                : config.getPersistence().getDatabase().getUrl();
        if (runId == null || runId.isBlank() || url == null || url.isBlank()) {
            return null;
        }
        String user = config.getPersistence().getDatabase().getUser();
        String password = config.getPersistence().getDatabase().getPassword();
        return new Thread(() -> {
            if (runFinished.get()) {
                return;
            }
            markRunStoppedIfRunning(url, user, password, runId);
        }, "edaf-run-shutdown-stop-hook-" + runId);
    }

    private static void markRunStoppedIfRunning(String url, String user, String password, String runId) {
        DataSource dataSource = null;
        try {
            dataSource = DataSourceFactory.create(url, user, password);
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                         UPDATE runs
                         SET status = 'STOPPED',
                             end_time = COALESCE(end_time, ?),
                             error_message = COALESCE(error_message, ?)
                         WHERE run_id = ?
                           AND UPPER(COALESCE(status, '')) = 'RUNNING'
                         """)) {
                statement.setString(1, Instant.now().toString());
                statement.setString(2, "Interrupted by process shutdown (Ctrl+C / termination signal)");
                statement.setString(3, runId);
                statement.executeUpdate();
            }
        } catch (Exception ignored) {
            // Best-effort cleanup during shutdown; no further action required on failure.
        } finally {
            closeDataSourceQuietly(dataSource);
        }
    }

    private static boolean isDbSinkEnabled(ExperimentConfig config) {
        if (config.getPersistence() == null || !config.getPersistence().isEnabled()) {
            return false;
        }
        if (config.getPersistence().getDatabase() == null || !config.getPersistence().getDatabase().isEnabled()) {
            return false;
        }
        return containsIgnoreCase(config.getPersistence().getSinks(), "db")
                || (config.getLogging() != null && containsIgnoreCase(config.getLogging().getModes(), "db"));
    }

    private static boolean containsIgnoreCase(List<String> values, String target) {
        if (values == null || target == null) {
            return false;
        }
        for (String value : values) {
            if (value != null && value.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    private static void removeShutdownHookQuietly(Thread hook) {
        if (hook == null) {
            return;
        }
        try {
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (IllegalStateException ignored) {
            // JVM is already shutting down; hook execution is expected.
        }
    }

    private static void closeDataSourceQuietly(DataSource dataSource) {
        if (dataSource instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // no-op
            }
        }
    }
}
