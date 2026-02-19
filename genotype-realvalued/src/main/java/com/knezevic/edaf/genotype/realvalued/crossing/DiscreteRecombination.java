package com.knezevic.edaf.genotype.realvalued.crossing;

import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.genotype.realvalued.RealValuedIndividual;

import java.util.Random;

/**
 * Discrete recombination: randomly selects each gene from one parent.
 * Independently selects object variables and strategy parameters.
 */
public class DiscreteRecombination implements Crossover<RealValuedIndividual> {

    private final Random random;

    public DiscreteRecombination(Random random) {
        this.random = random;
    }

    @Override
    public RealValuedIndividual crossover(RealValuedIndividual parent1, RealValuedIndividual parent2) {
        double[] g1 = parent1.getGenotype();
        double[] g2 = parent2.getGenotype();
        double[] s1 = parent1.getSigmas();
        double[] s2 = parent2.getSigmas();
        int n = g1.length;

        double[] childGenotype = new double[n];
        double[] childSigmas = new double[n];

        for (int i = 0; i < n; i++) {
            if (random.nextBoolean()) {
                childGenotype[i] = g1[i];
                childSigmas[i] = s1[i];
            } else {
                childGenotype[i] = g2[i];
                childSigmas[i] = s2[i];
            }
        }

        return new RealValuedIndividual(childGenotype, childSigmas);
    }
}
