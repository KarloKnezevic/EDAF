package com.knezevic.edaf.v3.core.metrics;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.LatentTelemetry;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.ModelDiagnostics;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Population;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.core.rng.RngStream;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for representation-specific latent metrics.
 */
class LatentKnowledgeAnalyzerTest {

    @Test
    void binaryEntropyIsOneForUniformBitMarginals() {
        Population<BinaryG> population = new Population<>(ObjectiveSense.MAXIMIZE);
        population.add(individual(new BinaryG(new boolean[]{true, false}), 1.0));
        population.add(individual(new BinaryG(new boolean[]{false, true}), 1.0));

        LatentTelemetry telemetry = LatentKnowledgeAnalyzer.analyze(
                population,
                population.asList(),
                new DummyModel<>(),
                LatentTelemetry.empty(),
                Map.of("latentDependencyEnabled", false)
        );

        assertEquals("binary", telemetry.representationFamily());
        assertEquals(1.0, telemetry.metrics().get("binary_mean_entropy"), 1.0e-9);
        assertEquals(0.0, telemetry.metrics().get("binary_fixation_ratio"), 1.0e-9);
        assertEquals(0.5, telemetry.metrics().get("binary_mean_probability"), 1.0e-9);
    }

    @Test
    void permutationConsensusDriftMatchesReversal() {
        Population<PermG> population = new Population<>(ObjectiveSense.MINIMIZE);
        population.add(individual(new PermG(new int[]{0, 1, 2, 3}), 0.0));
        population.add(individual(new PermG(new int[]{0, 1, 2, 3}), 0.0));

        LatentTelemetry previous = new LatentTelemetry(
                "permutation",
                Map.of(),
                Map.of("consensusPermutation", List.of(3, 2, 1, 0)),
                Map.of(),
                Map.of()
        );

        LatentTelemetry telemetry = LatentKnowledgeAnalyzer.analyze(
                population,
                population.asList(),
                new DummyModel<>(),
                previous,
                Map.of()
        );

        assertEquals("permutation", telemetry.representationFamily());
        assertEquals(1.0, telemetry.drift().get("consensus_kendall"), 1.0e-9);
        assertEquals(0.0, telemetry.metrics().get("perm_position_entropy_mean"), 1.0e-9);
        assertEquals(List.of(0, 1, 2, 3), telemetry.insights().get("consensusPermutation"));
    }

    @Test
    void gaussianKlDriftForMeanShiftMatchesClosedForm() {
        Population<RealG> population = new Population<>(ObjectiveSense.MINIMIZE);
        population.add(individual(new RealG(new double[]{1.0, 0.0}), 1.0));
        population.add(individual(new RealG(new double[]{1.0, 0.0}), 1.0));

        RealModel model = new RealModel(
                new double[]{1.0, 0.0},
                new double[]{1.0, 1.0},
                new double[][]{{1.0, 0.0}, {0.0, 1.0}}
        );
        LatentTelemetry previous = new LatentTelemetry(
                "real",
                Map.of(),
                Map.of(
                        "meanVector", List.of(0.0, 0.0),
                        "sigmaVector", List.of(1.0, 1.0)
                ),
                Map.of(),
                Map.of()
        );

        LatentTelemetry telemetry = LatentKnowledgeAnalyzer.analyze(
                population,
                population.asList(),
                model,
                previous,
                Map.of()
        );

        assertEquals("real", telemetry.representationFamily());
        assertEquals(0.5, telemetry.drift().get("gaussian_kl_diag"), 1.0e-9);
        assertEquals(0.0, telemetry.drift().get("sigma_l2"), 1.0e-9);
        assertTrue(telemetry.metrics().get("real_sigma_mean") > 0.0);
    }

    private static <T> Individual<T> individual(T genotype, double fitness) {
        return new Individual<>(genotype, new ScalarFitness(fitness));
    }

    private record BinaryG(boolean[] genes) {
    }

    private record PermG(int[] order) {
    }

    private record RealG(double[] values) {
    }

    private static class DummyModel<G> implements Model<G> {
        @Override
        public String name() {
            return "dummy";
        }

        @Override
        public void fit(List<Individual<G>> selected, Representation<G> representation, RngStream rng) {
            // no-op in tests
        }

        @Override
        public List<G> sample(int count,
                              Representation<G> representation,
                              Problem<G> problem,
                              ConstraintHandling<G> constraintHandling,
                              RngStream rng) {
            return List.of();
        }

        @Override
        public ModelDiagnostics diagnostics() {
            return ModelDiagnostics.empty();
        }
    }

    /**
     * Real-valued test model exposing Gaussian parameters through bean-like accessors.
     */
    private static final class RealModel extends DummyModel<RealG> {
        private final double[] mean;
        private final double[] sigma;
        private final double[][] covariance;

        private RealModel(double[] mean, double[] sigma, double[][] covariance) {
            this.mean = mean;
            this.sigma = sigma;
            this.covariance = covariance;
        }

        public double[] mean() {
            return mean;
        }

        public double[] sigma() {
            return sigma;
        }

        public double[][] covariance() {
            return covariance;
        }
    }
}
