package com.knezevic.edaf.testing.problems;

import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.genotype.fp.FpIndividual;

public class Sphere implements Problem<FpIndividual> {

    @Override
    public void evaluate(FpIndividual individual) {
        double[] genotype = individual.getGenotype();
        double fitness = 0;
        for (double v : genotype) {
            fitness += v * v;
        }
        individual.setFitness(fitness);
    }
}
