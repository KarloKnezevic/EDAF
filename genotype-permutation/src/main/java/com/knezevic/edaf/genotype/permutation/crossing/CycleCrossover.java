package com.knezevic.edaf.genotype.permutation.crossing;

import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

import java.util.HashMap;
import java.util.Map;

/**
 * Cycle Crossover (CX).
 */
public class CycleCrossover implements Crossover<PermutationIndividual> {

    @Override
    public PermutationIndividual crossover(PermutationIndividual parent1, PermutationIndividual parent2) {
        int[] p1 = parent1.getGenotype();
        int[] p2 = parent2.getGenotype();
        int length = p1.length;
        int[] offspring = new int[length];
        boolean[] visited = new boolean[length];

        // Pre-compute index map for O(1) lookup of gene positions in p1
        Map<Integer, Integer> p1Index = new HashMap<>();
        for (int i = 0; i < length; i++) {
            p1Index.put(p1[i], i);
        }

        // Identify cycle positions
        boolean[] inCycle = new boolean[length];
        int index = 0;
        while (!visited[index]) {
            visited[index] = true;
            inCycle[index] = true;
            int gene = p2[index];
            Integer nextIndex = p1Index.get(gene);
            if (nextIndex == null) break;
            index = nextIndex;
        }

        // Build offspring: cycle positions from p1, rest from p2
        for (int i = 0; i < length; i++) {
            offspring[i] = inCycle[i] ? p1[i] : p2[i];
        }

        return new PermutationIndividual(offspring);
    }
}
