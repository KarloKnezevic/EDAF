package com.knezevic.edaf.v3.models.permutation;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.core.api.defaults.IdentityConstraintHandling;
import com.knezevic.edaf.v3.core.rng.RngManager;
import com.knezevic.edaf.v3.repr.impl.PermutationVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.PermutationVector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests EHM sampling validity.
 */
class EdgeHistogramModelTest {

    @Test
    void sampledPermutationsRemainValid() {
        EdgeHistogramModel model = new EdgeHistogramModel(1e-6);
        PermutationVectorRepresentation representation = new PermutationVectorRepresentation(5);

        List<Individual<PermutationVector>> selected = List.of(
                new Individual<>(new PermutationVector(new int[]{0, 1, 2, 3, 4}), new ScalarFitness(10.0)),
                new Individual<>(new PermutationVector(new int[]{0, 2, 1, 4, 3}), new ScalarFitness(9.0)),
                new Individual<>(new PermutationVector(new int[]{1, 0, 2, 4, 3}), new ScalarFitness(8.0))
        );

        RngManager rng = new RngManager(2026L);
        model.fit(selected, representation, rng.stream("fit"));

        List<PermutationVector> samples = model.sample(200, representation, null, new IdentityConstraintHandling<>(), rng.stream("sample"));
        for (PermutationVector sample : samples) {
            assertTrue(representation.isValid(sample));
        }
    }
}
