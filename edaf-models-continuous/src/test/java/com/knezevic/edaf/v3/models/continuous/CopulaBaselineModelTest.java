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
 * Tests Gaussian copula model with empirical marginals.
 */
class CopulaBaselineModelTest {

    @Test
    void copulaCapturesRankDependenceAndSamplesDeterministically() {
        CopulaBaselineModel model = new CopulaBaselineModel(1.0e-9);
        RealVectorRepresentation representation = new RealVectorRepresentation(3, -10.0, 10.0);

        List<Individual<RealVector>> selected = new ArrayList<>();
        for (int i = 0; i < 90; i++) {
            double x0 = (i - 45.0) / 12.0;
            double x1 = 0.8 * x0 + Math.sin(i * 0.1) * 0.2;
            double x2 = -0.6 * x0 + Math.cos(i * 0.13) * 0.15;
            selected.add(new Individual<>(new RealVector(new double[]{x0, x1, x2}), new ScalarFitness(i)));
        }

        RngManager first = new RngManager(456L);
        model.fit(selected, representation, first.stream("fit"));
        List<RealVector> sampleA = model.sample(70, representation, null, new IdentityConstraintHandling<>(), first.stream("sample"));

        CopulaBaselineModel second = new CopulaBaselineModel(1.0e-9);
        RngManager secondRng = new RngManager(456L);
        second.fit(selected, representation, secondRng.stream("fit"));
        List<RealVector> sampleB = second.sample(70, representation, null, new IdentityConstraintHandling<>(), secondRng.stream("sample"));

        assertEquals(sampleA.stream().map(RealVector::toString).toList(), sampleB.stream().map(RealVector::toString).toList());
        assertTrue(model.diagnostics().numeric().getOrDefault("copula_rank_dependence", 0.0) > 0.05);
    }
}
