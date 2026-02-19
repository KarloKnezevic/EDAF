package com.knezevic.edaf.v3.models.discrete;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.core.api.defaults.IdentityConstraintHandling;
import com.knezevic.edaf.v3.core.rng.RngManager;
import com.knezevic.edaf.v3.repr.impl.BitStringRepresentation;
import com.knezevic.edaf.v3.repr.types.BitString;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests basic probabilistic behavior of UMDA Bernoulli model.
 */
class BernoulliUmdaModelTest {

    @Test
    void fitProducesValidProbabilitiesAndDeterministicSampling() {
        BernoulliUmdaModel model = new BernoulliUmdaModel(0.01);
        BitStringRepresentation representation = new BitStringRepresentation(4);

        List<Individual<BitString>> selected = List.of(
                new Individual<>(new BitString(new boolean[]{true, true, false, false}), new ScalarFitness(3.0)),
                new Individual<>(new BitString(new boolean[]{true, false, false, false}), new ScalarFitness(2.0)),
                new Individual<>(new BitString(new boolean[]{true, true, true, false}), new ScalarFitness(4.0))
        );

        RngManager rng = new RngManager(11L);
        model.fit(selected, representation, rng.stream("fit"));

        for (double p : model.probabilities()) {
            assertTrue(p > 0.0 && p < 1.0);
        }

        List<BitString> samplesA = model.sample(20, representation, null, new IdentityConstraintHandling<>(), rng.stream("sample-a"));
        RngManager rng2 = new RngManager(11L);
        model.fit(selected, representation, rng2.stream("fit"));
        List<BitString> samplesB = model.sample(20, representation, null, new IdentityConstraintHandling<>(), rng2.stream("sample-a"));

        assertEquals(
                samplesA.stream().map(BitString::toString).toList(),
                samplesB.stream().map(BitString::toString).toList()
        );
    }
}
