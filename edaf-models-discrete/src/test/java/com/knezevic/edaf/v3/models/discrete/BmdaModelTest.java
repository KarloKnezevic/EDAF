package com.knezevic.edaf.v3.models.discrete;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.core.api.defaults.IdentityConstraintHandling;
import com.knezevic.edaf.v3.core.rng.RngManager;
import com.knezevic.edaf.v3.repr.impl.BitStringRepresentation;
import com.knezevic.edaf.v3.repr.types.BitString;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests BMDA dependency-tree learning and deterministic sampling.
 */
class BmdaModelTest {

    @Test
    void learnsDependenciesAndSamplesDeterministically() {
        BmdaModel model = new BmdaModel(0.5);
        BitStringRepresentation representation = new BitStringRepresentation(6);

        List<Individual<BitString>> selected = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            boolean leader = (i % 4) != 0;
            boolean[] genes = new boolean[]{
                    leader,
                    leader,
                    (i % 3) == 0,
                    leader,
                    (i % 5) < 2,
                    (i % 2) == 0
            };
            selected.add(new Individual<>(new BitString(genes), new ScalarFitness(i)));
        }

        RngManager first = new RngManager(11L);
        model.fit(selected, representation, first.stream("fit"));
        List<BitString> sampleA = model.sample(50, representation, null, new IdentityConstraintHandling<>(), first.stream("sample"));

        BmdaModel second = new BmdaModel(0.5);
        RngManager secondRng = new RngManager(11L);
        second.fit(selected, representation, secondRng.stream("fit"));
        List<BitString> sampleB = second.sample(50, representation, null, new IdentityConstraintHandling<>(), secondRng.stream("sample"));

        assertEquals(sampleA.stream().map(BitString::toString).toList(), sampleB.stream().map(BitString::toString).toList());
        assertTrue(model.diagnostics().numeric().getOrDefault("bmda_dependency_edges", 0.0) >= 1.0);
    }
}
