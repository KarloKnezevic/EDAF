package com.knezevic.edaf.genotype.permutation;

import com.knezevic.edaf.genotype.permutation.crossing.CycleCrossover;
import com.knezevic.edaf.genotype.permutation.crossing.OrderCrossover;
import com.knezevic.edaf.genotype.permutation.crossing.PartiallyMappedCrossover;
import com.knezevic.edaf.genotype.permutation.mutation.*;
import net.jqwik.api.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PermutationOperatorProperties {

    @Provide
    Arbitrary<int[]> permutations() {
        return Arbitraries.integers().between(4, 20).map(len -> {
            int[] perm = new int[len];
            for (int i = 0; i < len; i++) perm[i] = i + 1;
            // Fisher-Yates shuffle
            Random rng = new Random();
            for (int i = len - 1; i > 0; i--) {
                int j = rng.nextInt(i + 1);
                int tmp = perm[i];
                perm[i] = perm[j];
                perm[j] = tmp;
            }
            return perm;
        });
    }

    private void assertValidPermutation(int[] genotype, int expectedLength) {
        assertEquals(expectedLength, genotype.length);
        Set<Integer> seen = new HashSet<>();
        for (int gene : genotype) {
            assertTrue(seen.add(gene), "Duplicate gene: " + gene + " in " + Arrays.toString(genotype));
        }
    }

    // --- Crossover properties ---

    @Property(tries = 200)
    void pmxProducesValidPermutation(@ForAll("permutations") int[] g1) {
        int[] g2 = g1.clone();
        // Shuffle g2 to create a different permutation with same elements
        Random rng = new Random();
        for (int i = g2.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = g2[i];
            g2[i] = g2[j];
            g2[j] = tmp;
        }

        PartiallyMappedCrossover crossover = new PartiallyMappedCrossover(rng);
        PermutationIndividual offspring = crossover.crossover(
            new PermutationIndividual(g1), new PermutationIndividual(g2));

        assertValidPermutation(offspring.getGenotype(), g1.length);
    }

    @Property(tries = 200)
    void orderCrossoverProducesValidPermutation(@ForAll("permutations") int[] g1) {
        int[] g2 = g1.clone();
        Random rng = new Random();
        for (int i = g2.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = g2[i];
            g2[i] = g2[j];
            g2[j] = tmp;
        }

        OrderCrossover crossover = new OrderCrossover(rng);
        PermutationIndividual offspring = crossover.crossover(
            new PermutationIndividual(g1), new PermutationIndividual(g2));

        assertValidPermutation(offspring.getGenotype(), g1.length);
    }

    @Property(tries = 200)
    void cycleCrossoverProducesValidPermutation(@ForAll("permutations") int[] g1) {
        int[] g2 = g1.clone();
        Random rng = new Random();
        for (int i = g2.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = g2[i];
            g2[i] = g2[j];
            g2[j] = tmp;
        }

        CycleCrossover crossover = new CycleCrossover();
        PermutationIndividual offspring = crossover.crossover(
            new PermutationIndividual(g1), new PermutationIndividual(g2));

        assertValidPermutation(offspring.getGenotype(), g1.length);
    }

    // --- Mutation properties ---

    @Property(tries = 200)
    void swapMutationProducesValidPermutation(@ForAll("permutations") int[] genotype) {
        PermutationIndividual individual = new PermutationIndividual(genotype.clone());
        new SwapMutation(new Random()).mutate(individual);
        assertValidPermutation(individual.getGenotype(), genotype.length);
    }

    @Property(tries = 200)
    void insertMutationProducesValidPermutation(@ForAll("permutations") int[] genotype) {
        PermutationIndividual individual = new PermutationIndividual(genotype.clone());
        new InsertMutation(new Random()).mutate(individual);
        assertValidPermutation(individual.getGenotype(), genotype.length);
    }

    @Property(tries = 200)
    void scrambleMutationProducesValidPermutation(@ForAll("permutations") int[] genotype) {
        PermutationIndividual individual = new PermutationIndividual(genotype.clone());
        new ScrambleMutation(new Random()).mutate(individual);
        assertValidPermutation(individual.getGenotype(), genotype.length);
    }

    @Property(tries = 200)
    void inversionMutationProducesValidPermutation(@ForAll("permutations") int[] genotype) {
        PermutationIndividual individual = new PermutationIndividual(genotype.clone());
        new InversionMutation(new Random()).mutate(individual);
        assertValidPermutation(individual.getGenotype(), genotype.length);
    }

    @Property(tries = 200)
    void shiftMutationProducesValidPermutation(@ForAll("permutations") int[] genotype) {
        PermutationIndividual individual = new PermutationIndividual(genotype.clone());
        new ShiftMutation(new Random()).mutate(individual);
        assertValidPermutation(individual.getGenotype(), genotype.length);
    }
}
