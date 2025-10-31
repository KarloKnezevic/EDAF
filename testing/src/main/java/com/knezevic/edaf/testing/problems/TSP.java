package com.knezevic.edaf.testing.problems;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

import java.util.List;
import java.util.Map;

public class TSP implements Problem<PermutationIndividual> {
    private final double[][] dist;
    private final OptimizationType optimizationType = OptimizationType.min;

    public TSP(Map<String, Object> params) {
        Object matrix = params.get("distanceMatrix");
        if (matrix instanceof List<?> rows) {
            int n = rows.size();
            dist = new double[n][n];
            for (int i = 0; i < n; i++) {
                List<?> row = (List<?>) rows.get(i);
                for (int j = 0; j < n; j++) {
                    dist[i][j] = ((Number) row.get(j)).doubleValue();
                }
            }
        } else {
            int n = ((Number) params.getOrDefault("n", 20)).intValue();
            java.util.Random rnd = new java.util.Random(((Number) params.getOrDefault("seed", 42)).longValue());
            double[][] coords = new double[n][2];
            for (int i = 0; i < n; i++) {
                coords[i][0] = rnd.nextDouble();
                coords[i][1] = rnd.nextDouble();
            }
            dist = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    double dx = coords[i][0] - coords[j][0];
                    double dy = coords[i][1] - coords[j][1];
                    dist[i][j] = Math.hypot(dx, dy);
                }
            }
        }
    }

    @Override
    public void evaluate(PermutationIndividual individual) {
        int[] p = individual.getGenotype();
        double total = 0.0;
        for (int i = 0; i < p.length; i++) {
            int a = p[i];
            int b = p[(i + 1) % p.length];
            total += dist[a][b];
        }
        individual.setFitness(total);
    }

    @Override
    public OptimizationType getOptimizationType() {
        return optimizationType;
    }
}


