package com.knezevic.edaf.v3.models.continuous;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.core.api.defaults.IdentityConstraintHandling;
import com.knezevic.edaf.v3.core.rng.RngManager;
import com.knezevic.edaf.v3.repr.impl.RealVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.RealVector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression and invariants for CMA-ES strategy model implementation.
 */
class CmaEsStrategyModelTest {

    @Test
    void samplesRemainFiniteAndInsideRepresentationAfterFit() {
        CmaEsStrategyModel model = new CmaEsStrategyModel(1e-12, 5.0, 1e-12, 0.8);
        RealVectorRepresentation representation = new RealVectorRepresentation(4, -5.0, 5.0);

        List<Individual<RealVector>> selected = List.of(
                individual(new double[]{0.0, 1.0, -1.0, 0.5}),
                individual(new double[]{0.5, 1.5, -0.5, 0.25}),
                individual(new double[]{-0.5, 0.8, -1.2, 0.1}),
                individual(new double[]{0.1, 1.2, -0.7, 0.4})
        );

        RngManager rng = new RngManager(42L);
        model.fit(selected, representation, rng.stream("fit"));

        List<RealVector> samples = model.sample(
                300,
                representation,
                null,
                new IdentityConstraintHandling<>(),
                rng.stream("sample")
        );

        for (RealVector sample : samples) {
            assertTrue(representation.isValid(sample));
            for (double value : sample.values()) {
                assertTrue(Double.isFinite(value));
            }
        }
    }

    @Test
    void restoreProducesDeterministicSamplingWithSameRng() {
        CmaEsStrategyModel original = new CmaEsStrategyModel(1e-12, 5.0, 1e-12, 0.6);
        RealVectorRepresentation representation = new RealVectorRepresentation(3, -5.0, 5.0);

        List<Individual<RealVector>> selected = List.of(
                individual(new double[]{0.3, -0.1, 0.2}),
                individual(new double[]{0.2, -0.2, 0.1}),
                individual(new double[]{0.4, -0.05, 0.15}),
                individual(new double[]{0.1, -0.3, 0.05})
        );

        RngManager fitRng = new RngManager(7L);
        original.fit(selected, representation, fitRng.stream("fit"));

        CmaEsStrategyModel restored = new CmaEsStrategyModel();
        restored.restore(
                original.mean(),
                original.sigma(),
                original.covariance(),
                original.pathSigma(),
                original.pathCovariance(),
                original.generation()
        );

        RngManager rngA = new RngManager(99L);
        RngManager rngB = new RngManager(99L);

        List<RealVector> a = original.sample(50, representation, null, new IdentityConstraintHandling<>(), rngA.stream("sample"));
        List<RealVector> b = restored.sample(50, representation, null, new IdentityConstraintHandling<>(), rngB.stream("sample"));

        for (int i = 0; i < a.size(); i++) {
            assertArrayEquals(a.get(i).values(), b.get(i).values(), 1e-12);
        }
    }

    @Test
    void simpleSphereLoopImprovesBestFitness() {
        CmaEsStrategyModel model = new CmaEsStrategyModel(1e-12, 5.0, 1e-12, 1.2);
        RealVectorRepresentation representation = new RealVectorRepresentation(5, -5.0, 5.0);
        IdentityConstraintHandling<RealVector> constraint = new IdentityConstraintHandling<>();
        RngManager rng = new RngManager(1234L);

        int lambda = 64;
        int mu = 32;
        List<Individual<RealVector>> population = new ArrayList<>();
        for (int i = 0; i < lambda; i++) {
            RealVector candidate = representation.random(rng.stream("init"));
            population.add(individual(candidate.values()));
        }

        double initialBest = bestFitness(population);

        for (int iteration = 0; iteration < 45; iteration++) {
            population.sort(Comparator.comparingDouble(i -> i.fitness().scalar()));
            List<Individual<RealVector>> selected = new ArrayList<>(population.subList(0, mu));
            model.fit(selected, representation, rng.stream("fit"));

            List<RealVector> sampled = model.sample(lambda, representation, null, constraint, rng.stream("sample"));
            population = new ArrayList<>(sampled.stream().map(v -> individual(v.values())).toList());
        }

        double finalBest = bestFitness(population);
        assertTrue(finalBest < initialBest);
        assertTrue(finalBest < 1.0);
    }

    @Test
    void restartLogicTriggersUnderStagnation() {
        CmaEsStrategyModel model = new CmaEsStrategyModel(
                1e-12,
                5.0,
                1e-12,
                0.3,
                true,
                2,
                2.0,
                1.0e12,
                1.0e-12
        );
        RealVectorRepresentation representation = new RealVectorRepresentation(3, -5.0, 5.0);

        List<Individual<RealVector>> selected = List.of(
                individual(new double[]{0.2, -0.1, 0.3}),
                individual(new double[]{0.25, -0.08, 0.28}),
                individual(new double[]{0.22, -0.12, 0.26}),
                individual(new double[]{0.24, -0.11, 0.27})
        );

        RngManager rng = new RngManager(111L);
        model.fit(selected, representation, rng.stream("fit-0"));
        double sigmaBefore = model.sigma();

        model.fit(selected, representation, rng.stream("fit-1"));
        model.fit(selected, representation, rng.stream("fit-2"));
        model.fit(selected, representation, rng.stream("fit-3"));

        assertTrue(model.restartCount() >= 1);
        assertTrue(model.sigma() >= sigmaBefore);
    }

    private static double bestFitness(List<Individual<RealVector>> population) {
        return population.stream().mapToDouble(i -> i.fitness().scalar()).min().orElse(Double.POSITIVE_INFINITY);
    }

    private static Individual<RealVector> individual(double[] values) {
        RealVector vector = new RealVector(values.clone());
        return new Individual<>(vector, new ScalarFitness(sphere(values)));
    }

    private static double sphere(double[] values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value * value;
        }
        return sum;
    }
}
