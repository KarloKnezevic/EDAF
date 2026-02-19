package com.knezevic.edaf.genotype.categorical;

import com.knezevic.edaf.core.api.Individual;
import java.util.Arrays;

/**
 * Individual with categorical genotype where gene i is an index in [0, cardinality[i]).
 * Suitable for hyperparameter tuning, configuration optimization, and discrete search spaces.
 */
public class CategoricalIndividual implements Individual<int[]> {

    private final int[] genotype;
    private double fitness;

    public CategoricalIndividual(int[] genotype) {
        this.genotype = genotype;
    }

    @Override
    public double getFitness() { return fitness; }

    @Override
    public void setFitness(double fitness) { this.fitness = fitness; }

    @Override
    public int[] getGenotype() { return genotype; }

    @Override
    public CategoricalIndividual copy() {
        CategoricalIndividual copy = new CategoricalIndividual(Arrays.copyOf(genotype, genotype.length));
        copy.setFitness(fitness);
        return copy;
    }
}
