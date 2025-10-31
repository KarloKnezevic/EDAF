package com.knezevic.edaf.testing.problems.misc;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Map;

public class RastriginProblem extends AbstractProblem<FpIndividual> {
    private final OptimizationType optimizationType = OptimizationType.min;
    public RastriginProblem(Map<String, Object> params) { super(params); }

    @Override
    public void evaluate(FpIndividual individual) {
        double[] x = individual.getGenotype();
        double sum = 0.0;
        for (double v : x) {
            sum += (v * v - 10.0 * Math.cos(2 * Math.PI * v) + 10.0);
        }
        individual.setFitness(sum);
    }

    @Override
    public OptimizationType getOptimizationType() { return optimizationType; }
}

