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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests separable NES update behavior.
 */
class SNesModelTest {

    @Test
    void snesImprovesSphereFitnessInSimpleLoop() {
        SNesModel model = new SNesModel(0.5, 0.2, 1.0e-8, 10.0);
        RealVectorRepresentation representation = new RealVectorRepresentation(5, -6.0, 6.0);
        IdentityConstraintHandling<RealVector> constraint = new IdentityConstraintHandling<>();
        RngManager rng = new RngManager(6789L);

        int lambda = 60;
        int mu = 30;
        List<Individual<RealVector>> population = new ArrayList<>();
        for (int i = 0; i < lambda; i++) {
            RealVector candidate = representation.random(rng.stream("init"));
            population.add(new Individual<>(candidate, new ScalarFitness(sphere(candidate.values()))));
        }
        double initialBest = population.stream().mapToDouble(i -> i.fitness().scalar()).min().orElse(Double.POSITIVE_INFINITY);

        for (int iteration = 0; iteration < 40; iteration++) {
            population.sort(Comparator.comparingDouble(i -> i.fitness().scalar()));
            List<Individual<RealVector>> selected = new ArrayList<>(population.subList(0, mu));
            model.fit(selected, representation, rng.stream("fit"));
            List<RealVector> sample = model.sample(lambda, representation, null, constraint, rng.stream("sample"));
            population = new ArrayList<>(sample.stream()
                    .map(v -> new Individual<>(v, new ScalarFitness(sphere(v.values()))))
                    .toList());
        }

        double finalBest = population.stream().mapToDouble(i -> i.fitness().scalar()).min().orElse(Double.POSITIVE_INFINITY);
        assertTrue(finalBest < initialBest);
        assertTrue(model.diagnostics().numeric().getOrDefault("nes_gradient_norm", 0.0) >= 0.0);
    }

    private static double sphere(double[] values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value * value;
        }
        return sum;
    }
}
