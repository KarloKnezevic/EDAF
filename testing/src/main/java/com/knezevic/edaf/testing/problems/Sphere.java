package com.knezevic.edaf.testing.problems;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Map;

public class Sphere extends AbstractProblem<FpIndividual> {

    public Sphere(Map<String, Object> params) {
        super(params);
    }

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
