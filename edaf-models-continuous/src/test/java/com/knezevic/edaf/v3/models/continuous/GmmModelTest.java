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
 * Tests GMM model EM fitting and deterministic mixture sampling.
 */
class GmmModelTest {

    @Test
    void gmmSamplingIsDeterministicAndFinite() {
        GmmModel model = new GmmModel(3, 15, 1.0e-8);
        RealVectorRepresentation representation = new RealVectorRepresentation(3, -10.0, 10.0);

        List<Individual<RealVector>> selected = new ArrayList<>();
        for (int i = 0; i < 90; i++) {
            double[] values;
            if (i < 30) {
                values = new double[]{-3.0 + 0.1 * i, -2.0 + 0.05 * i, 1.0};
            } else if (i < 60) {
                values = new double[]{2.0 + 0.05 * (i - 30), 3.0 - 0.08 * (i - 30), -1.0};
            } else {
                values = new double[]{0.5 + 0.04 * (i - 60), -3.5 + 0.1 * (i - 60), 2.5};
            }
            selected.add(new Individual<>(new RealVector(values), new ScalarFitness(i)));
        }

        RngManager first = new RngManager(123L);
        model.fit(selected, representation, first.stream("fit"));
        List<RealVector> sampleA = model.sample(80, representation, null, new IdentityConstraintHandling<>(), first.stream("sample"));

        GmmModel second = new GmmModel(3, 15, 1.0e-8);
        RngManager secondRng = new RngManager(123L);
        second.fit(selected, representation, secondRng.stream("fit"));
        List<RealVector> sampleB = second.sample(80, representation, null, new IdentityConstraintHandling<>(), secondRng.stream("sample"));

        assertEquals(sampleA.stream().map(RealVector::toString).toList(), sampleB.stream().map(RealVector::toString).toList());
        assertTrue(model.diagnostics().numeric().getOrDefault("gmm_log_likelihood", Double.NEGATIVE_INFINITY) > Double.NEGATIVE_INFINITY);
    }
}
