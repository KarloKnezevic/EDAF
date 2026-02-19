package com.knezevic.edaf.genotype.categorical.mutation;

import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.genotype.categorical.CategoricalIndividual;
import java.util.Random;

/**
 * Random reset mutation for categorical individuals.
 * Each gene has a probability of being reset to a random valid category.
 */
public class CategoricalRandomResetMutation implements Mutation<CategoricalIndividual> {

    private final Random random;
    private final int[] cardinalities;
    private final double mutationRate;

    public CategoricalRandomResetMutation(Random random, int[] cardinalities, double mutationRate) {
        this.random = random;
        this.cardinalities = cardinalities;
        this.mutationRate = mutationRate;
    }

    @Override
    public void mutate(CategoricalIndividual individual) {
        int[] genotype = individual.getGenotype();
        for (int i = 0; i < genotype.length; i++) {
            if (random.nextDouble() < mutationRate) {
                genotype[i] = random.nextInt(cardinalities[i]);
            }
        }
    }
}
