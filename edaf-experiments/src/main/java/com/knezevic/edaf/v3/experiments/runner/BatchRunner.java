package com.knezevic.edaf.v3.experiments.runner;

import com.knezevic.edaf.v3.core.config.BatchConfig;
import com.knezevic.edaf.v3.core.config.ConfigLoader;
import com.knezevic.edaf.v3.core.events.EventSink;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        for (BatchConfig.BatchExperimentEntry experiment : batch.getExperiments()) {
            Path configPath = batchConfigPath.getParent() == null
                    ? Path.of(experiment.getConfig())
                    : batchConfigPath.getParent().resolve(experiment.getConfig()).normalize();

            int repetitions = Math.max(1, experiment.getRepetitions());
            for (int repetition = 0; repetition < repetitions; repetition++) {
                var loaded = configLoader.load(configPath);
                var config = loaded.config();

                if (repetitions > 1 || hasText(experiment.getRunIdPrefix())) {
                    String baseId = hasText(experiment.getRunIdPrefix())
                            ? experiment.getRunIdPrefix().trim()
                            : config.getRun().getId();
                    config.getRun().setId(baseId + "-r" + String.format(Locale.ROOT, "%02d", repetition + 1));
                }

                long baseSeed = experiment.getSeedStart() != null
                        ? experiment.getSeedStart()
                        : config.getRun().getMasterSeed();
                config.getRun().setMasterSeed(baseSeed + repetition);

                results.add(runner.run(config, additionalSinks));
            }
        }
        return results;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
