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
 * Tests sparse Bayesian-network BOA/EBNA model behavior.
 */
class BoaEbnaModelTest {

    @Test
    void fitsSparseNetworkAndSamplesDeterministically() {
        BoaEbnaModel model = new BoaEbnaModel(3, 0.5);
        BitStringRepresentation representation = new BitStringRepresentation(7);

        List<Individual<BitString>> selected = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            boolean a = (i % 4) != 0;
            boolean b = a;
            boolean c = !a;
            boolean d = b || (i % 5 == 0);
            boolean e = c ^ ((i % 2) == 0);
            boolean f = d && !e;
            boolean g = (i % 3) == 0;
            selected.add(new Individual<>(new BitString(new boolean[]{a, b, c, d, e, f, g}), new ScalarFitness(i)));
        }

        RngManager first = new RngManager(77L);
        model.fit(selected, representation, first.stream("fit"));
        List<BitString> sampleA = model.sample(50, representation, null, new IdentityConstraintHandling<>(), first.stream("sample"));

        BoaEbnaModel second = new BoaEbnaModel(3, 0.5);
        RngManager secondRng = new RngManager(77L);
        second.fit(selected, representation, secondRng.stream("fit"));
        List<BitString> sampleB = second.sample(50, representation, null, new IdentityConstraintHandling<>(), secondRng.stream("sample"));

        assertEquals(sampleA.stream().map(BitString::toString).toList(), sampleB.stream().map(BitString::toString).toList());
        assertTrue(model.diagnostics().numeric().getOrDefault("boa_network_edges", 0.0) > 0.0);
    }
}
