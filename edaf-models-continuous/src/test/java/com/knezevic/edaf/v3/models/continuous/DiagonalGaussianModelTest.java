package com.knezevic.edaf.v3.models.continuous;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.core.api.defaults.IdentityConstraintHandling;
import com.knezevic.edaf.v3.core.rng.RngManager;
import com.knezevic.edaf.v3.repr.impl.RealVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.RealVector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests key Gaussian model invariants for continuous EDAs.
 */
class DiagonalGaussianModelTest {

    @Test
    void samplesRemainFiniteAndInsideRepresentationAfterRepair() {
        DiagonalGaussianModel model = new DiagonalGaussianModel(1e-6);
        RealVectorRepresentation representation = new RealVectorRepresentation(3, -5.0, 5.0);

        List<Individual<RealVector>> selected = List.of(
                new Individual<>(new RealVector(new double[]{0.0, 1.0, -1.0}), new ScalarFitness(1.0)),
                new Individual<>(new RealVector(new double[]{1.0, 2.0, -2.0}), new ScalarFitness(2.0)),
                new Individual<>(new RealVector(new double[]{-1.0, 0.5, -0.5}), new ScalarFitness(0.5))
        );

        RngManager rng = new RngManager(99L);
        model.fit(selected, representation, rng.stream("fit"));
        List<RealVector> samples = model.sample(200, representation, null, new IdentityConstraintHandling<>(), rng.stream("sample"));

        for (RealVector sample : samples) {
            assertTrue(representation.isValid(sample));
            for (double value : sample.values()) {
                assertTrue(Double.isFinite(value));
            }
        }
    }
}
