package com.knezevic.edaf.v3.experiments;

import com.knezevic.edaf.v3.core.config.ExperimentConfig;

import java.nio.file.Path;
import java.util.List;

/**
 * Shared test utility for building minimal runnable experiment configs.
 */
final class TestConfigFactory {

    private TestConfigFactory() {
        // utility class
    }

    static ExperimentConfig baseConfig(String runId, Path outDir) {
        ExperimentConfig config = new ExperimentConfig();
        config.getRun().setId(runId);
        config.getRun().setMasterSeed(12345L);

        config.getRepresentation().setType("bitstring");
        config.getProblem().setType("onemax");
        config.getAlgorithm().setType("umda");
        config.getModel().setType("umda-bernoulli");
        config.getSelection().setType("truncation");
        config.getReplacement().setType("elitist");
        config.getConstraints().setType("identity");
        config.getLocalSearch().setType("none");
        config.getRestart().setType("none");
        config.getNiching().setType("none");

        config.getStopping().setType("max-iterations");
        config.getStopping().setMaxIterations(50);

        config.getPersistence().setEnabled(true);
        config.getPersistence().setSinks(List.of("csv", "jsonl"));
        config.getPersistence().setOutputDirectory(outDir.toString());
        config.getPersistence().getDatabase().setEnabled(false);

        config.getReporting().setEnabled(false);
        config.getWeb().setEnabled(false);
        config.getLogging().setModes(List.of("jsonl"));
        config.getLogging().setVerbosity("normal");

        return config;
    }
}
