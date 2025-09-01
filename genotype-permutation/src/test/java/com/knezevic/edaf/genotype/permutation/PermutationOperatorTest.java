package com.knezevic.edaf.genotype.permutation;

import com.knezevic.edaf.genotype.permutation.crossing.CycleCrossover;
import com.knezevic.edaf.genotype.permutation.crossing.OrderCrossover;
import com.knezevic.edaf.genotype.permutation.crossing.PartiallyMappedCrossover;
import com.knezevic.edaf.genotype.permutation.mutation.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PermutationOperatorTest {

    @Test
    void testPartiallyMappedCrossover() {
        Random random = new Random();
        PartiallyMappedCrossover crossover = new PartiallyMappedCrossover(random);

        PermutationIndividual parent1 = new PermutationIndividual(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        PermutationIndividual parent2 = new PermutationIndividual(new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1});

        PermutationIndividual offspring = crossover.crossover(parent1, parent2);
        assertNotNull(offspring);
        assertEquals(parent1.getGenotype().length, offspring.getGenotype().length);
        Set<Integer> genes = new HashSet<>();
        for (int gene : offspring.getGenotype()) {
            genes.add(gene);
        }
        assertEquals(parent1.getGenotype().length, genes.size());
    }

    @Test
    void testOrderCrossover() {
        Random random = new Random();
        OrderCrossover crossover = new OrderCrossover(random);

        PermutationIndividual parent1 = new PermutationIndividual(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        PermutationIndividual parent2 = new PermutationIndividual(new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1});

        PermutationIndividual offspring = crossover.crossover(parent1, parent2);
        assertNotNull(offspring);
        assertEquals(parent1.getGenotype().length, offspring.getGenotype().length);
        Set<Integer> genes = new HashSet<>();
        for (int gene : offspring.getGenotype()) {
            genes.add(gene);
        }
        assertEquals(parent1.getGenotype().length, genes.size());
    }

    @Test
    void testCycleCrossover() {
        CycleCrossover crossover = new CycleCrossover();

        PermutationIndividual parent1 = new PermutationIndividual(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        PermutationIndividual parent2 = new PermutationIndividual(new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1});

        PermutationIndividual offspring = crossover.crossover(parent1, parent2);
        assertNotNull(offspring);
        assertEquals(parent1.getGenotype().length, offspring.getGenotype().length);
        Set<Integer> genes = new HashSet<>();
        for (int gene : offspring.getGenotype()) {
            genes.add(gene);
        }
        assertEquals(parent1.getGenotype().length, genes.size());
    }

    @Test
    void testSwapMutation() {
        Random random = new Random();
        SwapMutation mutation = new SwapMutation(random);

        int[] original = {1, 2, 3, 4, 5};
        PermutationIndividual individual = new PermutationIndividual(original.clone());
        mutation.mutate(individual);
        assertNotEquals(Arrays.toString(original), Arrays.toString(individual.getGenotype()));
    }

    @Test
    void testInsertMutation() {
        Random random = new Random();
        InsertMutation mutation = new InsertMutation(random);

        int[] original = {1, 2, 3, 4, 5};
        PermutationIndividual individual = new PermutationIndividual(original.clone());
        mutation.mutate(individual);
        assertNotEquals(Arrays.toString(original), Arrays.toString(individual.getGenotype()));
    }

    @Test
    void testScrambleMutation() {
        Random random = new Random();
        ScrambleMutation mutation = new ScrambleMutation(random);

        int[] original = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        PermutationIndividual individual = new PermutationIndividual(original.clone());
        for (int i = 0; i < 10; i++) {
            mutation.mutate(individual);
            if (!Arrays.equals(original, individual.getGenotype())) {
                break;
            }
        }
        assertNotEquals(Arrays.toString(original), Arrays.toString(individual.getGenotype()));
    }

    @Test
    void testInversionMutation() {
        Random random = new Random();
        InversionMutation mutation = new InversionMutation(random);

        int[] original = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        PermutationIndividual individual = new PermutationIndividual(original.clone());
        mutation.mutate(individual);
        assertNotEquals(Arrays.toString(original), Arrays.toString(individual.getGenotype()));
    }

    @Test
    void testShiftMutation() {
        Random random = new Random();
        ShiftMutation mutation = new ShiftMutation(random);

        int[] original = {1, 2, 3, 4, 5};
        PermutationIndividual individual = new PermutationIndividual(original.clone());
        mutation.mutate(individual);
        assertNotEquals(Arrays.toString(original), Arrays.toString(individual.getGenotype()));
    }
}
