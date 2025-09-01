package com.knezevic.edaf.genotype.permutation.crossing;

import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

import java.util.Random;

/**
 * Partially Mapped Crossover (PMX).
 */
public class PartiallyMappedCrossover implements Crossover<PermutationIndividual> {

    private final Random random;

    public PartiallyMappedCrossover(Random random) {
        this.random = random;
    }

    @Override
    public PermutationIndividual crossover(PermutationIndividual parent1, PermutationIndividual parent2) {
        int[] p1 = parent1.getGenotype();
        int[] p2 = parent2.getGenotype();
        int length = p1.length;
        int[] offspring = new int[length];
        System.arraycopy(p1, 0, offspring, 0, length);

        int point1 = random.nextInt(length);
        int point2 = random.nextInt(length);
        if (point1 > point2) {
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }

        for (int i = point1; i <= point2; i++) {
            int gene = p2[i];
            int index = -1;
            for (int j = 0; j < length; j++) {
                if (offspring[j] == gene) {
                    index = j;
                    break;
                }
            }
            int temp = offspring[i];
            offspring[i] = offspring[index];
            offspring[index] = temp;
        }

        return new PermutationIndividual(offspring);
    }
}
