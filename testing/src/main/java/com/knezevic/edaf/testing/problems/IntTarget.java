package com.knezevic.edaf.testing.problems;

import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.genotype.integer.IntegerIndividual;

public class IntTarget implements Problem<IntegerIndividual> {

    private final int target;

    public IntTarget(int target) {
        this.target = target;
    }

    @Override
    public void evaluate(IntegerIndividual individual) {
        int[] genotype = individual.getGenotype();
        double fitness = 0;
        for (int gene : genotype) {
            fitness += Math.abs(gene - target);
        }
        individual.setFitness(fitness);
    }
}
