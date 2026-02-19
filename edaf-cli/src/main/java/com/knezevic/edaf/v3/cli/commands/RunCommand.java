package com.knezevic.edaf.v3.cli.commands;

import com.knezevic.edaf.v3.cli.ui.ConsoleUiSink;
import com.knezevic.edaf.v3.cli.ui.Verbosity;
import com.knezevic.edaf.v3.cli.logging.LoggingConfigurator;
import com.knezevic.edaf.v3.core.config.ConfigLoader;
import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.experiments.runner.ExperimentRunner;
import com.knezevic.edaf.v3.experiments.runner.RunExecution;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

        List<EventSink> extraSinks = new ArrayList<>();
        if (config.getLogging().getModes().stream().map(String::toLowerCase).anyMatch("console"::equals)) {
            extraSinks.add(new ConsoleUiSink(
                    effectiveVerbosity,
                    config.getStopping().getMaxIterations(),
                    config.getObservability().getMetricsEveryIterations()
            ));
        }

        ExperimentRunner runner = new ExperimentRunner();
        RunExecution execution = runner.run(config, extraSinks);

        for (String warning : loaded.warnings()) {
            System.out.println("[WARN] " + warning);
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

        return 0;
    }
}
