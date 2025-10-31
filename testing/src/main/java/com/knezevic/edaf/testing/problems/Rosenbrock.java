package com.knezevic.edaf.testing.problems;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Map;

public class Rosenbrock implements Problem<FpIndividual> {
    private final OptimizationType optimizationType = OptimizationType.min;

    public Rosenbrock(Map<String, Object> params) { }

    @Override
    public void evaluate(FpIndividual ind) {
        double[] x = ind.getGenotype();
        double sum = 0.0;
        for (int i = 0; i < x.length - 1; i++) {
            double a = x[i+1] - x[i]*x[i];
            double b = 1 - x[i];
            sum += 100*a*a + b*b;
        }
        ind.setFitness(sum);
    }

    @Override
    public OptimizationType getOptimizationType() {
        return optimizationType;
    }
}


