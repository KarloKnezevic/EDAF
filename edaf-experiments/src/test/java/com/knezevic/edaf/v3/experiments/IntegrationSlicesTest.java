package com.knezevic.edaf.v3.experiments;

import com.knezevic.edaf.v3.core.config.ExperimentConfig;
import com.knezevic.edaf.v3.experiments.runner.ExperimentRunner;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

        ExperimentConfig config = TestConfigFactory.baseConfig("int-umda", outDir);
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

        ExperimentConfig config = TestConfigFactory.baseConfig("int-gaussian", outDir);
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
    void cmaEsOnSphereResumeIsDeterministic() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-cma");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("int-cma", outDir);
        config.getRepresentation().setType("real-vector");
        config.getRepresentation().getParams().put("length", 10);
        config.getRepresentation().getParams().put("lower", -5.0);
        config.getRepresentation().getParams().put("upper", 5.0);
        config.getProblem().setType("sphere");
        config.getAlgorithm().setType("cma-es");
        config.getAlgorithm().getParams().put("populationSize", 64);
        config.getAlgorithm().getParams().put("selectionRatio", 0.5);
        config.getAlgorithm().getParams().put("elitism", 0);
        config.getModel().setType("cma-es");
        config.getModel().getParams().put("initialSigma", 1.2);
        config.getModel().getParams().put("minSigma", 1.0e-12);
        config.getSelection().setType("truncation");
        config.getStopping().setMaxIterations(35);
        config.getRun().setCheckpointEveryIterations(10);

        var baseline = runner.run(config, List.of());
        Path checkpoint = outDir.resolve("checkpoints").resolve("int-cma-iter-20.ckpt.yaml");
        assertTrue(Files.exists(checkpoint));

        var resumed = runner.resume(checkpoint, List.of());
        assertEquals(baseline.result().best().fitness().scalar(), resumed.result().best().fitness().scalar(), 1e-9);
    }

    @Test
    void ehmOnSmallTspProducesMetrics() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-tsp");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("int-ehm", outDir);
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

    @Test
    void umdaOnKnapsackImprovesFitness() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-knapsack");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("int-knapsack", outDir);
        config.getRepresentation().setType("bitstring");
        config.getRepresentation().getParams().put("length", 20);
        config.getProblem().setType("knapsack");
        config.getProblem().getParams().put("capacity", 78);
        config.getAlgorithm().setType("umda");
        config.getAlgorithm().getParams().put("populationSize", 160);
        config.getAlgorithm().getParams().put("selectionRatio", 0.4);
        config.getModel().setType("umda-bernoulli");
        config.getSelection().setType("truncation");
        config.getStopping().setMaxIterations(90);

        var result = runner.run(config, List.of());
        assertTrue(result.result().best().fitness().scalar() > 90.0);
    }

    @Test
    void umdaOnMaxSatFindsHighClauseCoverage() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-maxsat");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("int-maxsat", outDir);
        config.getRepresentation().setType("bitstring");
        config.getRepresentation().getParams().put("length", 20);
        config.getProblem().setType("maxsat");
        config.getProblem().getParams().put("instance", "classpath:maxsat/uf20-01.cnf");
        config.getAlgorithm().setType("umda");
        config.getAlgorithm().getParams().put("populationSize", 180);
        config.getAlgorithm().getParams().put("selectionRatio", 0.45);
        config.getModel().setType("umda-bernoulli");
        config.getSelection().setType("truncation");
        config.getStopping().setMaxIterations(110);

        var result = runner.run(config, List.of());
        assertTrue(result.result().best().fitness().scalar() >= 45.0);
    }

    @Test
    void moSkeletonOnZdtProducesVectorFitness() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-zdt");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("int-zdt", outDir);
        config.getRepresentation().setType("real-vector");
        config.getRepresentation().getParams().put("length", 20);
        config.getRepresentation().getParams().put("lower", 0.0);
        config.getRepresentation().getParams().put("upper", 1.0);
        config.getProblem().setType("zdt");
        config.getProblem().getParams().put("functionId", 1);
        config.getAlgorithm().setType("mo-eda-skeleton");
        config.getAlgorithm().getParams().put("populationSize", 120);
        config.getAlgorithm().getParams().put("selectionRatio", 0.4);
        config.getModel().setType("gaussian-diag");
        config.getSelection().setType("truncation");
        config.getStopping().setMaxIterations(50);

        var result = runner.run(config, List.of());
        assertEquals(2, result.result().best().fitness().objectives().length);
    }

    @Test
    void treeEdaRunsNguyenSymbolicRegression() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-nguyen");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("int-nguyen", outDir);
        config.getRepresentation().setType("variable-length-vector");
        config.getRepresentation().getParams().put("minLength", 8);
        config.getRepresentation().getParams().put("maxLength", 24);
        config.getRepresentation().getParams().put("maxToken", 16);
        config.getProblem().setType("nguyen-sr");
        config.getProblem().getParams().put("variant", 1);
        config.getAlgorithm().setType("tree-eda");
        config.getAlgorithm().getParams().put("populationSize", 150);
        config.getAlgorithm().getParams().put("selectionRatio", 0.4);
        config.getModel().setType("token-categorical");
        config.getModel().getParams().put("maxToken", 16);
        config.getSelection().setType("truncation");
        config.getStopping().setMaxIterations(80);

        var result = runner.run(config, List.of());
        assertTrue(Double.isFinite(result.result().best().fitness().scalar()));
    }

    @Test
    void umdaRunsBooleanFunctionCryptoProblem() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-crypto-bf");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("int-crypto-bf", outDir);
        config.getRepresentation().setType("bitstring");
        config.getRepresentation().getParams().put("length", 32);
        config.getProblem().setType("boolean-function");
        config.getProblem().getParams().put("n", 5);
        config.getProblem().getParams().put("criteria", List.of("balancedness", "nonlinearity", "algebraic-degree"));
        config.getProblem().getParams().put("criterionWeights", java.util.Map.of(
                "balancedness", 0.35,
                "nonlinearity", 0.5,
                "algebraic-degree", 0.15
        ));
        config.getAlgorithm().setType("umda");
        config.getAlgorithm().getParams().put("populationSize", 180);
        config.getAlgorithm().getParams().put("selectionRatio", 0.45);
        config.getModel().setType("umda-bernoulli");
        config.getSelection().setType("truncation");
        config.getStopping().setMaxIterations(80);

        var result = runner.run(config, List.of());
        assertTrue(Double.isFinite(result.result().best().fitness().scalar()));
        assertTrue(result.result().best().fitness().scalar() > 0.4);
    }

    @Test
    void ehmRunsBooleanFunctionPermutationProblem() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-crypto-perm");
        ExperimentRunner runner = new ExperimentRunner();

        ExperimentConfig config = TestConfigFactory.baseConfig("int-crypto-perm", outDir);
        config.getRepresentation().setType("permutation-vector");
        config.getRepresentation().getParams().put("size", 32);
        config.getProblem().setType("boolean-function-permutation");
        config.getProblem().getParams().put("n", 5);
        config.getProblem().getParams().put("criteria", List.of("nonlinearity", "algebraic-degree"));
        config.getAlgorithm().setType("ehm-eda");
        config.getAlgorithm().getParams().put("populationSize", 160);
        config.getAlgorithm().getParams().put("selectionRatio", 0.5);
        config.getModel().setType("ehm");
        config.getSelection().setType("truncation");
        config.getStopping().setMaxIterations(70);

        var result = runner.run(config, List.of());
        assertTrue(Double.isFinite(result.result().best().fitness().scalar()));
        assertTrue(result.result().best().fitness().scalar() > 0.2);
    }

}
