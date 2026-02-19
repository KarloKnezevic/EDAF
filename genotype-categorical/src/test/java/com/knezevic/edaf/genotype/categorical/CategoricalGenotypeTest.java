package com.knezevic.edaf.genotype.categorical;

import com.knezevic.edaf.genotype.categorical.crossing.CategoricalUniformCrossover;
import com.knezevic.edaf.genotype.categorical.mutation.CategoricalRandomResetMutation;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CategoricalGenotypeTest {

    @Test
    void testGenotypeCreation() {
        Random random = new Random(42);
        int[] cardinalities = {3, 4, 2, 5, 3};
        CategoricalGenotype genotype = new CategoricalGenotype(cardinalities, random);
        int[] genes = genotype.create();

        assertEquals(5, genes.length);
        for (int i = 0; i < genes.length; i++) {
            assertTrue(genes[i] >= 0 && genes[i] < cardinalities[i],
                "Gene " + i + " must be in [0, " + cardinalities[i] + ")");
        }
    }

    @Test
    void testUniformCrossover() {
        Random random = new Random(42);
        int[] g1 = {0, 0, 0, 0, 0};
        int[] g2 = {2, 3, 1, 4, 2};

        CategoricalIndividual p1 = new CategoricalIndividual(g1);
        CategoricalIndividual p2 = new CategoricalIndividual(g2);

        CategoricalUniformCrossover crossover = new CategoricalUniformCrossover(random);
        CategoricalIndividual child = crossover.crossover(p1, p2);

        assertEquals(5, child.getGenotype().length);
        for (int i = 0; i < 5; i++) {
            assertTrue(child.getGenotype()[i] == g1[i] || child.getGenotype()[i] == g2[i],
                "Each gene must come from one parent");
        }
    }

    @Test
    void testRandomResetMutation() {
        Random random = new Random(42);
        int[] cardinalities = {3, 4, 2, 5, 3};
        int[] genotype = {0, 0, 0, 0, 0};
        CategoricalIndividual individual = new CategoricalIndividual(genotype);

        // High mutation rate to ensure changes
        CategoricalRandomResetMutation mutation = new CategoricalRandomResetMutation(random, cardinalities, 1.0);
        mutation.mutate(individual);

        for (int i = 0; i < genotype.length; i++) {
            assertTrue(individual.getGenotype()[i] >= 0 && individual.getGenotype()[i] < cardinalities[i],
                "Gene " + i + " must remain valid after mutation");
        }
    }

    @Test
    void testCopy() {
        int[] genotype = {1, 2, 0, 3, 1};
        CategoricalIndividual original = new CategoricalIndividual(genotype);
        original.setFitness(42.0);

        CategoricalIndividual copy = original.copy();
        assertEquals(original.getFitness(), copy.getFitness());
        assertArrayEquals(original.getGenotype(), copy.getGenotype());

        copy.getGenotype()[0] = 999;
        assertEquals(1, original.getGenotype()[0]);
    }

    @Test
    void testMultipleCreations() {
        Random random = new Random(42);
        int[] cardinalities = {10, 10, 10};
        CategoricalGenotype genotype = new CategoricalGenotype(cardinalities, random);

        // Create 100 individuals and verify all genes are valid
        for (int trial = 0; trial < 100; trial++) {
            int[] genes = genotype.create();
            for (int i = 0; i < genes.length; i++) {
                assertTrue(genes[i] >= 0 && genes[i] < cardinalities[i]);
            }
        }
    }
}
