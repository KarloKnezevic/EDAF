package com.knezevic.edaf.genotype.binary.mutation;

import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CompleteMixingMutationTest {

    @Test
    void testMutation() {
        Random random = new Random();
        CompleteMixingMutation mutation = new CompleteMixingMutation(random);

        byte[] originalGenotype = {0, 0, 0, 0, 0};
        BinaryIndividual individual = new BinaryIndividual(originalGenotype.clone());

        mutation.mutate(individual);

        byte[] mutatedGenotype = individual.getGenotype();

        // It's possible, but highly unlikely, that the mutated genotype is the same as the original
        assertNotEquals(originalGenotype, mutatedGenotype);
    }
}
