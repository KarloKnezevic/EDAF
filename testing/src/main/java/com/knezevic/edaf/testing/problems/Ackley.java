package com.knezevic.edaf.testing.problems;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Map;

public class Ackley implements Problem<FpIndividual> {
    private final OptimizationType optimizationType = OptimizationType.min;

    public Ackley(Map<String, Object> params) { }

    @Override
    public void evaluate(FpIndividual ind) {
        double[] x = ind.getGenotype();
        int n = x.length;
        double a = 20.0;
        double b = 0.2;
        double c = 2*Math.PI;
        double sumSq = 0.0;
        double sumCos = 0.0;
        for (double v : x) { sumSq += v*v; sumCos += Math.cos(c*v); }
        double term1 = -a * Math.exp(-b * Math.sqrt(sumSq / n));
        double term2 = -Math.exp(sumCos / n);
        double f = term1 + term2 + a + Math.E;
        ind.setFitness(f);
    }

    @Override
    public OptimizationType getOptimizationType() {
        return optimizationType;
    }
}


