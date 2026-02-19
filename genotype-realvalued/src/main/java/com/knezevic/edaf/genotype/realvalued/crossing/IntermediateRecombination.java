package com.knezevic.edaf.genotype.realvalued.crossing;

import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.genotype.realvalued.RealValuedIndividual;

/**
 * Intermediate (arithmetic mean) recombination for both object variables and strategy parameters.
 */
public class IntermediateRecombination implements Crossover<RealValuedIndividual> {

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
            childGenotype[i] = (g1[i] + g2[i]) / 2.0;
            childSigmas[i] = (s1[i] + s2[i]) / 2.0;
        }

        return new RealValuedIndividual(childGenotype, childSigmas);
    }
}
