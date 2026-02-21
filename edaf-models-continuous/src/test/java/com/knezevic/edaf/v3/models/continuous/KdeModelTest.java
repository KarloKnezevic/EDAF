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
 * Tests KDE model bandwidth estimation and deterministic sampling.
 */
class KdeModelTest {

    @Test
    void kdeProducesValidSamples() {
        KdeModel model = new KdeModel(1.0, 1.0e-8);
        RealVectorRepresentation representation = new RealVectorRepresentation(4, -8.0, 8.0);

        List<Individual<RealVector>> selected = new ArrayList<>();
        for (int i = 0; i < 75; i++) {
            double[] values = new double[]{
                    Math.sin(i * 0.2),
                    Math.cos(i * 0.15),
                    0.1 * (i - 30),
                    (i % 9) - 4.0
            };
            selected.add(new Individual<>(new RealVector(values), new ScalarFitness(i)));
        }

        RngManager first = new RngManager(321L);
        model.fit(selected, representation, first.stream("fit"));
        List<RealVector> sampleA = model.sample(60, representation, null, new IdentityConstraintHandling<>(), first.stream("sample"));

        KdeModel second = new KdeModel(1.0, 1.0e-8);
        RngManager secondRng = new RngManager(321L);
        second.fit(selected, representation, secondRng.stream("fit"));
        List<RealVector> sampleB = second.sample(60, representation, null, new IdentityConstraintHandling<>(), secondRng.stream("sample"));

        assertEquals(sampleA.stream().map(RealVector::toString).toList(), sampleB.stream().map(RealVector::toString).toList());
        assertTrue(model.diagnostics().numeric().getOrDefault("kde_bandwidth_mean", 0.0) > 0.0);
    }
}
