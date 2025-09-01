package com.knezevic.edaf.genotype.permutation.crossing;

import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

import java.util.ArrayList;
import java.util.List;

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

        List<Integer> cycle = new ArrayList<>();
        int index = 0;
        while (!visited[index]) {
            visited[index] = true;
            cycle.add(index);
            int gene = p2[index];
            index = -1;
            for (int i = 0; i < length; i++) {
                if (p1[i] == gene) {
                    index = i;
                    break;
                }
            }
        }

        for (int i = 0; i < length; i++) {
            if (cycle.contains(i)) {
                offspring[i] = p1[i];
            } else {
                offspring[i] = p2[i];
            }
        }

        return new PermutationIndividual(offspring);
    }
}
