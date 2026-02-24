/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.experiments;

import com.knezevic.edaf.v3.core.config.ExperimentConfig;
import com.knezevic.edaf.v3.core.runtime.ExecutionParallelism;
import com.knezevic.edaf.v3.experiments.runner.ExperimentRunner;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies deterministic behavior when dynamic worker budgets change due to concurrent run pressure.
 */
class ParallelFitnessDeterminismTest {

    @Test
    void umdaProducesSameResultWithAndWithoutWorkerPressure() throws Exception {
        Path baselineOut = Files.createTempDirectory("edaf-v3-parallel-baseline");
        Path pressuredOut = Files.createTempDirectory("edaf-v3-parallel-pressured");

        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig baselineConfig = deterministicUmdaConfig("parallel-baseline", baselineOut);
        double baselineBest = runner.run(baselineConfig, List.of()).result().best().fitness().scalar();

        List<ExecutionParallelism.RunLease> leases = new ArrayList<>();
        try {
            int pressure = Math.max(2, ExecutionParallelism.availableProcessors() * 2);
            for (int i = 0; i < pressure; i++) {
                leases.add(ExecutionParallelism.enterRun());
            }
            ExperimentConfig pressuredConfig = deterministicUmdaConfig("parallel-pressured", pressuredOut);
            double pressuredBest = runner.run(pressuredConfig, List.of()).result().best().fitness().scalar();
            assertEquals(baselineBest, pressuredBest, 1.0e-9,
                    "Result must stay deterministic regardless of parallel worker budget changes");
        } finally {
            for (int i = leases.size() - 1; i >= 0; i--) {
                leases.get(i).close();
            }
        }
    }

    private static ExperimentConfig deterministicUmdaConfig(String runId, Path outDir) {
        ExperimentConfig config = TestConfigFactory.baseConfig(runId, outDir);
        config.getRun().setMasterSeed(98127345L);
        config.getRun().setDeterministicStreams(true);
        config.getRepresentation().setType("bitstring");
        config.getRepresentation().getParams().put("length", 64);
        config.getProblem().setType("onemax");
        config.getAlgorithm().setType("umda");
        config.getAlgorithm().getParams().put("populationSize", 180);
        config.getAlgorithm().getParams().put("selectionRatio", 0.4);
        config.getModel().setType("umda-bernoulli");
        config.getModel().getParams().put("smoothing", 0.01);
        config.getStopping().setType("max-iterations");
        config.getStopping().setMaxIterations(90);
        return config;
    }
}
