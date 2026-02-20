package com.knezevic.edaf.v3.models.continuous;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.core.api.defaults.IdentityConstraintHandling;
import com.knezevic.edaf.v3.core.rng.RngManager;
import com.knezevic.edaf.v3.repr.impl.RealVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.RealVector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests flow-based model skew adaptation and deterministic sampling.
 */
class NormalizingFlowModelTest {

    @Test
    void flowAdaptsSkewAndSamplesDeterministically() {
        NormalizingFlowModel model = new NormalizingFlowModel(1e-9, 0.7, 1.0);
        RealVectorRepresentation representation = new RealVectorRepresentation(3, -10.0, 10.0);

        List<Individual<RealVector>> selected = new ArrayList<>();
        // Positively skewed input in first dimension.
        for (int i = 0; i < 80; i++) {
            double x0 = Math.pow((i + 1) / 80.0, 3.0) * 6.0;
            double x1 = (i % 7) - 3.0;
            double x2 = ((i * 2) % 9) - 4.0;
            selected.add(new Individual<>(new RealVector(new double[]{x0, x1, x2}), new ScalarFitness(i)));
        }

        RngManager first = new RngManager(77L);
        model.fit(selected, representation, first.stream("fit"));
        List<RealVector> sampleA = model.sample(60, representation, null, new IdentityConstraintHandling<>(), first.stream("sample"));

        NormalizingFlowModel second = new NormalizingFlowModel(1e-9, 0.7, 1.0);
        RngManager secondRng = new RngManager(77L);
        second.fit(selected, representation, secondRng.stream("fit"));
        List<RealVector> sampleB = second.sample(60, representation, null, new IdentityConstraintHandling<>(), secondRng.stream("sample"));

        assertEquals(
                sampleA.stream().map(RealVector::toString).toList(),
                sampleB.stream().map(RealVector::toString).toList()
        );
        assertTrue(second.diagnostics().numeric().getOrDefault("flow_skew_abs_max", 0.0) > 0.0);
        for (RealVector sample : sampleA) {
            assertTrue(representation.isValid(sample));
        }
    }
}
