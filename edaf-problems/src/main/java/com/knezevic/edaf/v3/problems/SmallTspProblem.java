package com.knezevic.edaf.v3.problems;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Small TSP benchmark with configurable or default city coordinates.
 */
public final class SmallTspProblem implements Problem<PermutationVector> {

    private final double[][] coordinates;

    public SmallTspProblem(double[][] coordinates) {
        this.coordinates = coordinates == null || coordinates.length == 0
                ? defaultCoordinates()
                : coordinates;
    }

    @Override
    public String name() {
        return "small-tsp";
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    @Override
    public Fitness evaluate(PermutationVector genotype) {
        int[] order = genotype.order();
        double length = 0.0;
        for (int i = 0; i < order.length; i++) {
            int from = order[i];
            int to = order[(i + 1) % order.length];
            length += distance(coordinates[from], coordinates[to]);
        }
        return new ScalarFitness(length);
    }

    @Override
    public java.util.List<String> violations(PermutationVector genotype) {
        if (genotype.size() != coordinates.length) {
            return java.util.List.of("Permutation size must match city count");
        }
        return java.util.List.of();
    }

    private static double distance(double[] a, double[] b) {
        double dx = a[0] - b[0];
        double dy = a[1] - b[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static double[][] defaultCoordinates() {
        return new double[][]{
                {0.0, 0.0},
                {1.0, 5.0},
                {5.0, 2.0},
                {6.0, 6.0},
                {8.0, 3.0},
                {2.0, 9.0},
                {9.0, 9.0},
                {4.0, 7.0}
        };
    }
}
