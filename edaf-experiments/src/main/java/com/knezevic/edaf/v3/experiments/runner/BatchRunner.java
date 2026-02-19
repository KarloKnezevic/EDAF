package com.knezevic.edaf.v3.experiments.runner;

import com.knezevic.edaf.v3.core.config.BatchConfig;
import com.knezevic.edaf.v3.core.config.ConfigLoader;
import com.knezevic.edaf.v3.core.events.EventSink;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes batch configs sequentially for CI-friendly reproducibility.
 */
public final class BatchRunner {

    private final ConfigLoader configLoader;
    private final ExperimentRunner runner;

    public BatchRunner() {
        this.configLoader = new ConfigLoader();
        this.runner = new ExperimentRunner();
    }

    public List<RunExecution> runBatch(Path batchConfigPath, List<EventSink> additionalSinks) {
        BatchConfig batch = configLoader.loadBatch(batchConfigPath);
        List<RunExecution> results = new ArrayList<>();
        for (String experiment : batch.getExperiments()) {
            Path configPath = batchConfigPath.getParent() == null
                    ? Path.of(experiment)
                    : batchConfigPath.getParent().resolve(experiment).normalize();
            var loaded = configLoader.load(configPath);
            results.add(runner.run(loaded.config(), additionalSinks));
        }
        return results;
    }
}
