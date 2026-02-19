package com.knezevic.edaf.v3.cli.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.knezevic.edaf.v3.cli.ui.ConsoleUiSink;
import com.knezevic.edaf.v3.cli.ui.Verbosity;
import com.knezevic.edaf.v3.cli.logging.LoggingConfigurator;
import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.experiments.runner.ExperimentRunner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Resumes an experiment from checkpoint.
 */
@Command(name = "resume", description = "Resume run from checkpoint")
public final class ResumeCommand implements Callable<Integer> {

    @Option(names = "--checkpoint", required = true, description = "Checkpoint path")
    private Path checkpoint;

    @Option(names = "--verbosity", description = "Override verbosity: quiet|normal|verbose|debug")
    private String verbosity;

    @Override
    public Integer call() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        var root = mapper.readTree(checkpoint.toFile());
        int maxIterations = root.path("config").path("stopping").path("maxIterations").asInt(100);
        int summaryEvery = root.path("config").path("observability").path("metricsEveryIterations").asInt(1);
        String cfgVerbosity = root.path("config").path("logging").path("verbosity").asText("normal");
        if (verbosity != null && !verbosity.isBlank()) {
            cfgVerbosity = verbosity;
        }
        Verbosity effectiveVerbosity = Verbosity.from(cfgVerbosity);
        LoggingConfigurator.apply(effectiveVerbosity);

        List<EventSink> sinks = new ArrayList<>();
        sinks.add(new ConsoleUiSink(effectiveVerbosity, maxIterations, summaryEvery));

        ExperimentRunner runner = new ExperimentRunner();
        var execution = runner.resume(checkpoint, sinks);
        for (String warning : execution.warnings()) {
            System.out.println("[WARN] " + warning);
        }
        return 0;
    }
}
