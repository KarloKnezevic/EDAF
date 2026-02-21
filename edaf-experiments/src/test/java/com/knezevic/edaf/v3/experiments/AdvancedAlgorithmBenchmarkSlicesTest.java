package com.knezevic.edaf.v3.experiments;

import com.knezevic.edaf.v3.core.config.ExperimentConfig;
import com.knezevic.edaf.v3.experiments.runner.ExperimentRunner;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Dedicated benchmark slices for upgraded high-impact algorithms.
 */
class AdvancedAlgorithmBenchmarkSlicesTest {

    @Test
    void hboaOnOnemaxReachesHighFitness() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-hboa");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("bench-hboa", outDir);
        config.getRepresentation().setType("bitstring");
        config.getRepresentation().getParams().put("length", 40);
        config.getProblem().setType("onemax");
        config.getAlgorithm().setType("hboa");
        config.getAlgorithm().getParams().put("populationSize", 180);
        config.getAlgorithm().getParams().put("selectionRatio", 0.45);
        config.getModel().setType("hboa-network");
        config.getModel().getParams().put("smoothing", 0.5);
        config.getModel().getParams().put("minMutualInformation", 1e-4);
        config.getModel().getParams().put("learningRate", 0.8);
        config.getStopping().setMaxIterations(80);

        var result = runner.run(config, java.util.List.of());
        assertTrue(result.result().best().fitness().scalar() >= 30.0);
    }

    @Test
    void fullCovarianceEdaOnSphereImprovesSubstantially() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-fullcov");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("bench-fullcov", outDir);
        config.getRepresentation().setType("real-vector");
        config.getRepresentation().getParams().put("length", 6);
        config.getRepresentation().getParams().put("lower", -5.0);
        config.getRepresentation().getParams().put("upper", 5.0);
        config.getProblem().setType("sphere");
        config.getAlgorithm().setType("full-covariance-eda");
        config.getAlgorithm().getParams().put("populationSize", 100);
        config.getAlgorithm().getParams().put("selectionRatio", 0.4);
        config.getModel().setType("gaussian-full");
        config.getModel().getParams().put("learningRate", 0.8);
        config.getModel().getParams().put("shrinkage", 0.1);
        config.getModel().getParams().put("jitter", 1e-8);
        config.getStopping().setMaxIterations(70);

        var result = runner.run(config, java.util.List.of());
        assertTrue(result.result().best().fitness().scalar() < 15.0);
    }

    @Test
    void flowEdaOnSphereConvergesWithFiniteDiagnostics() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-flow");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("bench-flow", outDir);
        config.getRepresentation().setType("real-vector");
        config.getRepresentation().getParams().put("length", 6);
        config.getRepresentation().getParams().put("lower", -5.0);
        config.getRepresentation().getParams().put("upper", 5.0);
        config.getProblem().setType("sphere");
        config.getAlgorithm().setType("flow-eda");
        config.getAlgorithm().getParams().put("populationSize", 100);
        config.getAlgorithm().getParams().put("selectionRatio", 0.4);
        config.getModel().setType("normalizing-flow");
        config.getModel().getParams().put("learningRate", 0.75);
        config.getModel().getParams().put("maxSkew", 0.9);
        config.getModel().getParams().put("jitter", 1e-8);
        config.getStopping().setMaxIterations(70);

        var result = runner.run(config, java.util.List.of());
        assertTrue(result.result().best().fitness().scalar() < 20.0);
    }

    @Test
    void ehbsaOnSmallTspProducesFiniteTourLength() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-ehbsa");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("bench-ehbsa", outDir);
        config.getRepresentation().setType("permutation-vector");
        config.getRepresentation().getParams().put("size", 8);
        config.getProblem().setType("small-tsp");
        config.getAlgorithm().setType("ehbsa");
        config.getAlgorithm().getParams().put("populationSize", 120);
        config.getAlgorithm().getParams().put("selectionRatio", 0.4);
        config.getModel().setType("ehm");
        config.getModel().getParams().put("epsilon", 1e-6);
        config.getStopping().setMaxIterations(90);

        var result = runner.run(config, java.util.List.of());
        assertTrue(result.result().best().fitness().scalar() > 0.0);
        assertTrue(Double.isFinite(result.result().best().fitness().scalar()));
    }

    @Test
    void slidingWindowEdaOnOnemaxMaintainsHighFitness() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-sliding");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("bench-sliding", outDir);
        config.getRepresentation().setType("bitstring");
        config.getRepresentation().getParams().put("length", 48);
        config.getProblem().setType("onemax");
        config.getAlgorithm().setType("sliding-window-eda");
        config.getAlgorithm().getParams().put("populationSize", 160);
        config.getAlgorithm().getParams().put("selectionRatio", 0.5);
        config.getAlgorithm().getParams().put("windowSize", 8);
        config.getAlgorithm().getParams().put("adjustmentStep", 0.02);
        config.getModel().setType("pbil-frequency");
        config.getModel().getParams().put("learningRate", 0.2);
        config.getStopping().setMaxIterations(70);

        var result = runner.run(config, java.util.List.of());
        assertTrue(result.result().best().fitness().scalar() >= 30.0);
    }

    @Test
    void noisyResamplingEdaOnSphereStaysStable() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-noisy");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("bench-noisy", outDir);
        config.getRepresentation().setType("real-vector");
        config.getRepresentation().getParams().put("length", 5);
        config.getRepresentation().getParams().put("lower", -5.0);
        config.getRepresentation().getParams().put("upper", 5.0);
        config.getProblem().setType("sphere");
        config.getAlgorithm().setType("noisy-resampling-eda");
        config.getAlgorithm().getParams().put("populationSize", 110);
        config.getAlgorithm().getParams().put("selectionRatio", 0.5);
        config.getAlgorithm().getParams().put("resamples", 4);
        config.getModel().setType("gaussian-diag");
        config.getModel().getParams().put("minSigma", 1e-8);
        config.getStopping().setMaxIterations(70);

        var result = runner.run(config, java.util.List.of());
        assertTrue(Double.isFinite(result.result().best().fitness().scalar()));
        assertTrue(result.result().best().fitness().scalar() < 30.0);
    }
}
