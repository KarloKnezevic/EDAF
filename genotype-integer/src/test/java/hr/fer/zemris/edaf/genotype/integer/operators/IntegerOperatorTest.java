package hr.fer.zemris.edaf.genotype.integer.operators;

import hr.fer.zemris.edaf.genotype.integer.IntegerIndividual;
import hr.fer.zemris.edaf.genotype.integer.crossing.OnePointCrossover;
import hr.fer.zemris.edaf.genotype.integer.crossing.TwoPointCrossover;
import hr.fer.zemris.edaf.genotype.integer.mutation.SimpleIntegerMutation;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class IntegerOperatorTest {

    @Test
    void testOnePointCrossover() {
        Random random = new Random();
        OnePointCrossover crossover = new OnePointCrossover(random);

        int[] p1Genotype = {1, 1, 1, 1, 1};
        int[] p2Genotype = {2, 2, 2, 2, 2};
        IntegerIndividual parent1 = new IntegerIndividual(p1Genotype);
        IntegerIndividual parent2 = new IntegerIndividual(p2Genotype);

        for (int k = 0; k < 100; k++) { // Run multiple times for robustness
            IntegerIndividual offspring = crossover.crossover(parent1, parent2);
            int[] offspringGenotype = offspring.getGenotype();

            int crossoverPoint = -1;
            for (int i = 0; i < offspringGenotype.length; i++) {
                if (offspringGenotype[i] != p1Genotype[i]) {
                    crossoverPoint = i;
                    break;
                }
            }

            if (crossoverPoint != -1) {
                for (int i = 0; i < offspringGenotype.length; i++) {
                    if (i < crossoverPoint) {
                        assertEquals(p1Genotype[i], offspringGenotype[i]);
                    } else {
                        assertEquals(p2Genotype[i], offspringGenotype[i]);
                    }
                }
            } else {
                // This happens if crossover point is at the end
                assertArrayEquals(p1Genotype, offspringGenotype);
            }
        }
    }

    @Test
    void testTwoPointCrossover() {
        Random random = new Random();
        TwoPointCrossover crossover = new TwoPointCrossover(random);

        int[] p1Genotype = {1, 1, 1, 1, 1};
        int[] p2Genotype = {2, 2, 2, 2, 2};
        IntegerIndividual parent1 = new IntegerIndividual(p1Genotype);
        IntegerIndividual parent2 = new IntegerIndividual(p2Genotype);

        for (int k = 0; k < 100; k++) {
            IntegerIndividual offspring = crossover.crossover(parent1, parent2);
            int[] offspringGenotype = offspring.getGenotype();

            for (int i = 0; i < offspringGenotype.length; i++) {
                assertTrue(offspringGenotype[i] == 1 || offspringGenotype[i] == 2);
            }
        }
    }

    @Test
    void testSimpleIntegerMutation() {
        Random random = new Random();
        SimpleIntegerMutation mutation = new SimpleIntegerMutation(random, 1.0, 0, 100);

        int[] initialGenotype = {5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
        IntegerIndividual individual = new IntegerIndividual(initialGenotype.clone());

        mutation.mutate(individual);

        assertFalse(java.util.Arrays.equals(initialGenotype, individual.getGenotype()),
                "Mutation with 100% probability should change the genotype.");

        for (int gene : individual.getGenotype()) {
            assertTrue(gene >= 0 && gene <= 100, "Mutated gene " + gene + " is out of bounds.");
        }
    }
}
