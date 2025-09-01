package com.knezevic.edaf.genotype.permutation.crossing;

import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Order Crossover (OX).
 */
public class OrderCrossover implements Crossover<PermutationIndividual> {

    private final Random random;

    public OrderCrossover(Random random) {
        this.random = random;
    }

    @Override
    public PermutationIndividual crossover(PermutationIndividual parent1, PermutationIndividual parent2) {
        int[] p1 = parent1.getGenotype();
        int[] p2 = parent2.getGenotype();
        int length = p1.length;
        int[] offspring = new int[length];

        int point1 = random.nextInt(length);
        int point2 = random.nextInt(length);
        if (point1 > point2) {
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }

        Set<Integer> selected = new HashSet<>();
        for (int i = point1; i <= point2; i++) {
            offspring[i] = p1[i];
            selected.add(p1[i]);
        }

        int p2Index = 0;
        for (int i = 0; i < length; i++) {
            if (i >= point1 && i <= point2) {
                continue;
            }
            while (selected.contains(p2[p2Index])) {
                p2Index++;
            }
            offspring[i] = p2[p2Index];
            p2Index++;
        }

        return new PermutationIndividual(offspring);
    }
}
