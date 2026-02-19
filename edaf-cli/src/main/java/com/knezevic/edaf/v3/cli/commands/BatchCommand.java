package com.knezevic.edaf.v3.cli.commands;

import com.knezevic.edaf.v3.cli.logging.LoggingConfigurator;
import com.knezevic.edaf.v3.cli.ui.Verbosity;
import com.knezevic.edaf.v3.experiments.runner.BatchRunner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Executes a batch of experiment configs.
 */
@Command(name = "batch", description = "Run a batch configuration")
public final class BatchCommand implements Callable<Integer> {

    @Option(names = {"-c", "--config"}, required = true, description = "Path to batch YAML")
    private Path batchConfig;

    @Option(names = "--verbosity", description = "Override verbosity: quiet|normal|verbose|debug")
    private String verbosity;

    @Override
    public Integer call() {
        Verbosity effectiveVerbosity = verbosity != null && !verbosity.isBlank()
                ? Verbosity.from(verbosity)
                : Verbosity.NORMAL;
        LoggingConfigurator.apply(effectiveVerbosity);

        BatchRunner runner = new BatchRunner();
        var results = runner.runBatch(batchConfig, List.of());
        System.out.println("Completed batch runs: " + results.size());
        for (var result : results) {
            System.out.println("- " + result.result().runId() + " best=" + result.result().best().fitness().scalar());
        }
        return 0;
    }
}
