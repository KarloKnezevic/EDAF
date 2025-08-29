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
        Random random = new Random(42); // Use a fixed seed for deterministic testing
        OnePointCrossover crossover = new OnePointCrossover(random);

        IntegerIndividual parent1 = new IntegerIndividual(new int[]{1, 1, 1, 1, 1});
        IntegerIndividual parent2 = new IntegerIndividual(new int[]{2, 2, 2, 2, 2});

        // With seed 42, the first call to nextInt(5) returns 1.
        // Crossover point is 1. Offspring should be {1, 2, 2, 2, 2}.
        IntegerIndividual offspring = crossover.crossover(parent1, parent2);
        assertArrayEquals(new int[]{1, 2, 2, 2, 2}, offspring.getGenotype());
    }

    @Test
    void testTwoPointCrossover() {
        Random random = new Random(42); // Use a fixed seed
        TwoPointCrossover crossover = new TwoPointCrossover(random);

        IntegerIndividual parent1 = new IntegerIndividual(new int[]{1, 1, 1, 1, 1, 1, 1, 1});
        IntegerIndividual parent2 = new IntegerIndividual(new int[]{2, 2, 2, 2, 2, 2, 2, 2});

        // With seed 42, the first two calls to nextInt(8) are 1 and 5.
        // The points are swapped, so point1=1, point2=5.
        // Genes at indices 1, 2, 3, 4, 5 are from parent2.
        IntegerIndividual offspring = crossover.crossover(parent1, parent2);
        assertArrayEquals(new int[]{1, 2, 2, 2, 2, 2, 1, 1}, offspring.getGenotype());
    }

    @Test
    void testSimpleIntegerMutation() {
        Random random = new Random(42);
        // High probability to ensure mutation happens for testing
        SimpleIntegerMutation mutation = new SimpleIntegerMutation(random, 1.0, 0, 1000);

        int[] initialGenotype = {5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
        IntegerIndividual individual = new IntegerIndividual(initialGenotype.clone());

        mutation.mutate(individual);

        // With a large range, it's highly unlikely that all genes will mutate back to 5.
        assertFalse(java.util.Arrays.equals(initialGenotype, individual.getGenotype()),
                "Mutation with 100% probability should change the genotype.");

        // Check that values are within bounds
        for (int gene : individual.getGenotype()) {
            assertTrue(gene >= 0 && gene <= 1000, "Mutated gene " + gene + " is out of bounds.");
        }
    }
}
