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
 * Tests MIMIC Chow-Liu tree behavior.
 */
class MimicChowLiuModelTest {

    @Test
    void treeDepthAndSamplingAreStable() {
        MimicChowLiuModel model = new MimicChowLiuModel(0.4);
        BitStringRepresentation representation = new BitStringRepresentation(5);

        List<Individual<BitString>> selected = new ArrayList<>();
        for (int i = 0; i < 70; i++) {
            boolean a = (i % 3) != 0;
            boolean b = a;
            boolean c = (i % 2) == 0;
            boolean d = b && c;
            boolean e = (i % 7) < 3;
            selected.add(new Individual<>(new BitString(new boolean[]{a, b, c, d, e}), new ScalarFitness(i)));
        }

        RngManager first = new RngManager(51L);
        model.fit(selected, representation, first.stream("fit"));
        List<BitString> sampleA = model.sample(40, representation, null, new IdentityConstraintHandling<>(), first.stream("sample"));

        MimicChowLiuModel second = new MimicChowLiuModel(0.4);
        RngManager secondRng = new RngManager(51L);
        second.fit(selected, representation, secondRng.stream("fit"));
        List<BitString> sampleB = second.sample(40, representation, null, new IdentityConstraintHandling<>(), secondRng.stream("sample"));

        assertEquals(sampleA.stream().map(BitString::toString).toList(), sampleB.stream().map(BitString::toString).toList());
        assertTrue(model.diagnostics().numeric().getOrDefault("mimic_tree_depth", 0.0) >= 1.0);
    }
}
