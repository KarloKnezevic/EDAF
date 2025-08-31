package com.knezevic.edaf.genotype.binary.crossing;

import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UniformCrossoverTest {

    @Test
    void testCrossover() {
        Random random = new Random();
        UniformCrossover crossover = new UniformCrossover(random);

        byte[] p1Genotype = {1, 1, 1, 1, 1};
        byte[] p2Genotype = {0, 0, 0, 0, 0};
        BinaryIndividual parent1 = new BinaryIndividual(p1Genotype);
        BinaryIndividual parent2 = new BinaryIndividual(p2Genotype);

        BinaryIndividual offspring = crossover.crossover(parent1, parent2);
        byte[] offspringGenotype = offspring.getGenotype();

        for (int i = 0; i < offspringGenotype.length; i++) {
            byte gene = offspringGenotype[i];
            assertTrue(gene == p1Genotype[i] || gene == p2Genotype[i],
                    "Offspring gene at index " + i + " does not match either parent.");
        }
    }
}
