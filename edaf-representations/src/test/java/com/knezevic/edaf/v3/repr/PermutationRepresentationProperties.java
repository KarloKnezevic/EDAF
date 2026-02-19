package com.knezevic.edaf.v3.repr;

import com.knezevic.edaf.v3.core.rng.RngManager;
import com.knezevic.edaf.v3.repr.impl.PermutationVectorRepresentation;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property test verifying permutation sampler always produces valid permutations.
 */
class PermutationRepresentationProperties {

    @Property
    void generatedPermutationsAreValid(@ForAll @IntRange(min = 2, max = 40) int size) {
        PermutationVectorRepresentation representation = new PermutationVectorRepresentation(size);
        RngManager manager = new RngManager(1234L + size);
        for (int i = 0; i < 200; i++) {
            var sample = representation.random(manager.stream("repr"));
            assertTrue(representation.isValid(sample));
        }
    }
}
