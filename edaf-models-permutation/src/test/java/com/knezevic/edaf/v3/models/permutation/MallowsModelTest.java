package com.knezevic.edaf.v3.models.permutation;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.core.api.defaults.IdentityConstraintHandling;
import com.knezevic.edaf.v3.core.rng.RngManager;
import com.knezevic.edaf.v3.repr.impl.PermutationVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.PermutationVector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests Mallows-Kendall model fitting and permutation validity.
 */
class MallowsModelTest {

    @Test
    void samplesAreDeterministicAndValid() {
        MallowsModel model = new MallowsModel(0.05, 0.98);
        PermutationVectorRepresentation representation = new PermutationVectorRepresentation(6);

        List<Individual<PermutationVector>> selected = List.of(
                new Individual<>(new PermutationVector(new int[]{0, 1, 2, 3, 4, 5}), new ScalarFitness(1.0)),
                new Individual<>(new PermutationVector(new int[]{0, 2, 1, 3, 4, 5}), new ScalarFitness(1.2)),
                new Individual<>(new PermutationVector(new int[]{1, 0, 2, 3, 5, 4}), new ScalarFitness(1.3)),
                new Individual<>(new PermutationVector(new int[]{0, 1, 3, 2, 4, 5}), new ScalarFitness(1.1))
        );

        RngManager first = new RngManager(9191L);
        model.fit(selected, representation, first.stream("fit"));
        List<PermutationVector> sampleA = model.sample(80, representation, null, new IdentityConstraintHandling<>(), first.stream("sample"));

        MallowsModel second = new MallowsModel(0.05, 0.98);
        RngManager secondRng = new RngManager(9191L);
        second.fit(selected, representation, secondRng.stream("fit"));
        List<PermutationVector> sampleB = second.sample(80, representation, null, new IdentityConstraintHandling<>(), secondRng.stream("sample"));

        assertEquals(sampleA.stream().map(PermutationVector::toString).toList(), sampleB.stream().map(PermutationVector::toString).toList());
        for (PermutationVector sample : sampleA) {
            assertTrue(representation.isValid(sample));
        }
        assertTrue(model.diagnostics().numeric().getOrDefault("mallows_theta", 0.0) >= 0.0);
    }
}
