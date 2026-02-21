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
 * Tests xNES model deterministic sampling and diagnostics.
 */
class XNesModelTest {

    @Test
    void xnesSamplingIsDeterministic() {
        XNesModel model = new XNesModel(0.45, 0.12, 1.0e-9);
        RealVectorRepresentation representation = new RealVectorRepresentation(4, -8.0, 8.0);

        List<Individual<RealVector>> selected = List.of(
                new Individual<>(new RealVector(new double[]{-2.0, -1.0, 0.4, 0.8}), new ScalarFitness(1.0)),
                new Individual<>(new RealVector(new double[]{-1.2, -0.6, 0.9, 1.1}), new ScalarFitness(0.9)),
                new Individual<>(new RealVector(new double[]{1.4, 1.1, -0.7, -1.2}), new ScalarFitness(0.5)),
                new Individual<>(new RealVector(new double[]{2.1, 1.4, -1.0, -1.5}), new ScalarFitness(0.4))
        );

        RngManager first = new RngManager(98L);
        model.fit(selected, representation, first.stream("fit"));
        List<RealVector> sampleA = model.sample(70, representation, null, new IdentityConstraintHandling<>(), first.stream("sample"));

        XNesModel second = new XNesModel(0.45, 0.12, 1.0e-9);
        RngManager secondRng = new RngManager(98L);
        second.fit(selected, representation, secondRng.stream("fit"));
        List<RealVector> sampleB = second.sample(70, representation, null, new IdentityConstraintHandling<>(), secondRng.stream("sample"));

        assertEquals(sampleA.stream().map(RealVector::toString).toList(), sampleB.stream().map(RealVector::toString).toList());
        assertTrue(model.diagnostics().numeric().getOrDefault("xnes_update_step", 0.0) >= 0.0);
    }
}
