package hr.fer.zemris.edaf.genotype.integer;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class IntegerGenotypeTest {

    @Test
    void testCreate() {
        int length = 10;
        int min = -100;
        int max = 100;
        Random random = new Random();

        IntegerGenotype genotypeFactory = new IntegerGenotype(length, min, max, random);
        int[] genotype = genotypeFactory.create();

        assertEquals(length, genotype.length);

        for (int gene : genotype) {
            assertTrue(gene >= min && gene <= max, "Gene " + gene + " is out of bounds [" + min + ", " + max + "]");
        }
    }
}
