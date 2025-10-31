package com.knezevic.edaf.testing.problems.misc;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.Map;

public class DeceptiveTrapProblem extends AbstractProblem<BinaryIndividual> {
    private final OptimizationType optimizationType = OptimizationType.max;
    private final int k;

    public DeceptiveTrapProblem(Map<String, Object> params) {
        super(params);
        this.k = ((Number) params.getOrDefault("k", 5)).intValue();
    }

    @Override
    public void evaluate(BinaryIndividual individual) {
        byte[] g = individual.getGenotype();
        double fitness = 0.0;
        for (int i = 0; i < g.length; i += k) {
            int ones = 0;
            for (int j = i; j < Math.min(i + k, g.length); j++) ones += g[j];
            // Classic deceptive trap: f(ones) = k if ones==k else k-1-ones
            fitness += (ones == k) ? k : (k - 1 - ones);
        }
        individual.setFitness(fitness);
    }

    @Override
    public OptimizationType getOptimizationType() { return optimizationType; }
}

