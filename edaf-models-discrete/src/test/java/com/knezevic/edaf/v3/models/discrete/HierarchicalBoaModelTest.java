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
 * Tests structure learning and deterministic sampling for the hBOA model.
 */
class HierarchicalBoaModelTest {

    @Test
    void learnsDependencyEdgesOnCorrelatedBitstrings() {
        HierarchicalBoaModel model = new HierarchicalBoaModel(0.5, 1e-4, 0.9);
        BitStringRepresentation representation = new BitStringRepresentation(6);

        List<Individual<BitString>> selected = new ArrayList<>();
        // Construct a dataset where bits 1 and 2 are strongly tied to bit 0.
        for (int i = 0; i < 60; i++) {
            boolean leader = (i % 3) != 0;
            boolean[] genes = new boolean[]{
                    leader,
                    leader,
                    leader,
                    (i % 2) == 0,
                    (i % 5) == 0,
                    (i % 7) < 3
            };
            selected.add(new Individual<>(new BitString(genes), new ScalarFitness(i)));
        }

        model.fit(selected, representation, new RngManager(7L).stream("fit"));

        assertTrue(model.diagnostics().numeric().getOrDefault("hboa_edge_count", 0.0) >= 1.0);
        assertEquals(6, model.order().length);
    }

    @Test
    void samplingIsDeterministicForSameSeed() {
        HierarchicalBoaModel model = new HierarchicalBoaModel(0.5, 1e-4, 0.8);
        BitStringRepresentation representation = new BitStringRepresentation(4);

        List<Individual<BitString>> selected = List.of(
                new Individual<>(new BitString(new boolean[]{true, true, false, false}), new ScalarFitness(1.0)),
                new Individual<>(new BitString(new boolean[]{true, true, false, true}), new ScalarFitness(2.0)),
                new Individual<>(new BitString(new boolean[]{false, false, true, false}), new ScalarFitness(0.5)),
                new Individual<>(new BitString(new boolean[]{false, false, true, true}), new ScalarFitness(0.8))
        );

        RngManager firstRng = new RngManager(42L);
        model.fit(selected, representation, firstRng.stream("fit"));
        List<BitString> sampleA = model.sample(40, representation, null, new IdentityConstraintHandling<>(), firstRng.stream("sample"));

        HierarchicalBoaModel second = new HierarchicalBoaModel(0.5, 1e-4, 0.8);
        RngManager secondRng = new RngManager(42L);
        second.fit(selected, representation, secondRng.stream("fit"));
        List<BitString> sampleB = second.sample(40, representation, null, new IdentityConstraintHandling<>(), secondRng.stream("sample"));

        assertEquals(
                sampleA.stream().map(BitString::toString).toList(),
                sampleB.stream().map(BitString::toString).toList()
        );
    }
}
