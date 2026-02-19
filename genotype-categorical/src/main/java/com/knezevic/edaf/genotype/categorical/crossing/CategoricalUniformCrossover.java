package com.knezevic.edaf.genotype.categorical.crossing;

import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.genotype.categorical.CategoricalIndividual;
import java.util.Random;

/**
 * Uniform crossover for categorical individuals.
 * Each gene is randomly selected from one of the two parents.
 */
public class CategoricalUniformCrossover implements Crossover<CategoricalIndividual> {

    private final Random random;

    public CategoricalUniformCrossover(Random random) {
        this.random = random;
    }

    @Override
    public CategoricalIndividual crossover(CategoricalIndividual parent1, CategoricalIndividual parent2) {
        int[] g1 = parent1.getGenotype();
        int[] g2 = parent2.getGenotype();
        int n = g1.length;

        int[] childGenotype = new int[n];
        for (int i = 0; i < n; i++) {
            childGenotype[i] = random.nextBoolean() ? g1[i] : g2[i];
        }

        return new CategoricalIndividual(childGenotype);
    }
}
