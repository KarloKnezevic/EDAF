package com.knezevic.edaf.v3.experiments;

import com.knezevic.edaf.v3.core.config.ExperimentConfig;
import com.knezevic.edaf.v3.experiments.runner.ExperimentRunner;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end integration slices for discrete, continuous, and permutation pipelines.
 */
class IntegrationSlicesTest {

    @Test
    void umdaOnOneMaxConverges() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-umda");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = baseConfig("int-umda", outDir);
        config.getRepresentation().setType("bitstring");
        config.getRepresentation().getParams().put("length", 32);
        config.getProblem().setType("onemax");
        config.getAlgorithm().setType("umda");
        config.getAlgorithm().getParams().put("populationSize", 120);
        config.getAlgorithm().getParams().put("selectionRatio", 0.4);
        config.getModel().setType("umda-bernoulli");
        config.getSelection().setType("truncation");
        config.getStopping().setMaxIterations(80);

        var result = runner.run(config, List.of());
        assertTrue(result.result().best().fitness().scalar() >= 28.0);
    }

    @Test
    void gaussianDiagOnSphereResumeIsDeterministic() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-sphere");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = baseConfig("int-gaussian", outDir);
        config.getRepresentation().setType("real-vector");
        config.getRepresentation().getParams().put("length", 8);
        config.getRepresentation().getParams().put("lower", -5.0);
        config.getRepresentation().getParams().put("upper", 5.0);
        config.getProblem().setType("sphere");
        config.getAlgorithm().setType("gaussian-eda");
        config.getAlgorithm().getParams().put("populationSize", 80);
        config.getAlgorithm().getParams().put("selectionRatio", 0.4);
        config.getModel().setType("gaussian-diag");
        config.getSelection().setType("truncation");
        config.getStopping().setMaxIterations(40);
        config.getRun().setCheckpointEveryIterations(10);

        var baseline = runner.run(config, List.of());
        Path checkpoint = outDir.resolve("checkpoints").resolve("int-gaussian-iter-20.ckpt.yaml");
        assertTrue(Files.exists(checkpoint));

        var resumed = runner.resume(checkpoint, List.of());
        assertEquals(baseline.result().best().fitness().scalar(), resumed.result().best().fitness().scalar(), 1e-9);
    }

    @Test
    void ehmOnSmallTspProducesMetrics() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-tsp");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = baseConfig("int-ehm", outDir);
        config.getRepresentation().setType("permutation-vector");
        config.getRepresentation().getParams().put("size", 8);
        config.getProblem().setType("small-tsp");
        config.getAlgorithm().setType("ehm-eda");
        config.getAlgorithm().getParams().put("populationSize", 120);
        config.getAlgorithm().getParams().put("selectionRatio", 0.5);
        config.getModel().setType("ehm");
        config.getSelection().setType("truncation");
        config.getStopping().setMaxIterations(120);

        var result = runner.run(config, List.of());
        assertTrue(result.result().best().fitness().scalar() > 0.0);

        Path csv = outDir.resolve("int-ehm.csv");
        assertTrue(Files.exists(csv));
        assertTrue(Files.readAllLines(csv).size() > 10);
    }

    private static ExperimentConfig baseConfig(String runId, Path outDir) {
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
