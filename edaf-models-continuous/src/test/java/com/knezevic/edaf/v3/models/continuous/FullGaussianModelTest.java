package com.knezevic.edaf.v3.models.continuous;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.core.api.defaults.IdentityConstraintHandling;
import com.knezevic.edaf.v3.core.rng.RngManager;
import com.knezevic.edaf.v3.repr.impl.RealVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.RealVector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests adaptive full-covariance Gaussian model behavior.
 */
class FullGaussianModelTest {

    @Test
    void fitAndSampleRemainDeterministicAndFinite() {
        FullGaussianModel model = new FullGaussianModel(1e-9, 0.8, 0.15);
        RealVectorRepresentation representation = new RealVectorRepresentation(4, -8.0, 8.0);

        List<Individual<RealVector>> selected = List.of(
                new Individual<>(new RealVector(new double[]{-2.0, -1.0, 0.5, 1.0}), new ScalarFitness(1.0)),
                new Individual<>(new RealVector(new double[]{-1.5, -0.5, 1.0, 1.5}), new ScalarFitness(1.2)),
                new Individual<>(new RealVector(new double[]{1.0, 0.8, -0.3, -0.7}), new ScalarFitness(0.7)),
                new Individual<>(new RealVector(new double[]{2.3, 1.7, -0.8, -1.2}), new ScalarFitness(0.5))
        );

        RngManager first = new RngManager(100L);
        model.fit(selected, representation, first.stream("fit"));
        List<RealVector> sampleA = model.sample(80, representation, null, new IdentityConstraintHandling<>(), first.stream("sample"));

        FullGaussianModel second = new FullGaussianModel(1e-9, 0.8, 0.15);
        RngManager secondRng = new RngManager(100L);
        second.fit(selected, representation, secondRng.stream("fit"));
        List<RealVector> sampleB = second.sample(80, representation, null, new IdentityConstraintHandling<>(), secondRng.stream("sample"));

        assertEquals(
                sampleA.stream().map(RealVector::toString).toList(),
                sampleB.stream().map(RealVector::toString).toList()
        );
        for (RealVector vector : sampleA) {
            assertTrue(representation.isValid(vector));
        }
        assertTrue(second.diagnostics().numeric().getOrDefault("cov_condition_number", 0.0) > 0.0);
    }
}
