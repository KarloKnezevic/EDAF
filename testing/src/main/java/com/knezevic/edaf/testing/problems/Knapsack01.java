package com.knezevic.edaf.testing.problems;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.Map;

public class Knapsack01 implements Problem<BinaryIndividual> {
    private final int[] weights;
    private final int[] values;
    private final int capacity;
    private final OptimizationType optimizationType = OptimizationType.max;

    @SuppressWarnings("unchecked")
    public Knapsack01(Map<String, Object> params) {
        this.capacity = ((Number) params.getOrDefault("capacity", 50)).intValue();
        Object w = params.get("weights");
        Object v = params.get("values");
        if (w instanceof java.util.List<?> wl && v instanceof java.util.List<?> vl) {
            this.weights = wl.stream().mapToInt(o -> ((Number) o).intValue()).toArray();
            this.values = vl.stream().mapToInt(o -> ((Number) o).intValue()).toArray();
        } else {
            int n = ((Number) params.getOrDefault("items", 50)).intValue();
            java.util.Random rnd = new java.util.Random(((Number) params.getOrDefault("seed", 42)).longValue());
            this.weights = new int[n];
            this.values = new int[n];
            for (int i = 0; i < n; i++) {
                weights[i] = 1 + rnd.nextInt(10);
                values[i] = 1 + rnd.nextInt(10);
            }
        }
        if (weights.length != values.length) {
            throw new IllegalArgumentException("weights and values length mismatch");
        }
    }

    @Override
    public void evaluate(BinaryIndividual individual) {
        byte[] x = individual.getGenotype();
        int totalW = 0;
        int totalV = 0;
        for (int i = 0; i < Math.min(x.length, weights.length); i++) {
            if (x[i] == 1) {
                totalW += weights[i];
                totalV += values[i];
            }
        }
        if (totalW > capacity) {
            // penalize overweight
            totalV -= (totalW - capacity) * 10;
        }
        individual.setFitness(totalV);
    }

    @Override
    public OptimizationType getOptimizationType() {
        return optimizationType;
    }
}


