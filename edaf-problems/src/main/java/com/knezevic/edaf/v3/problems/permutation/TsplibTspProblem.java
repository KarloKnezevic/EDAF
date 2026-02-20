package com.knezevic.edaf.v3.problems.permutation;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.List;

/**
 * TSP problem backed by TSPLIB coordinates.
 */
public final class TsplibTspProblem implements Problem<PermutationVector> {

    private final String instanceName;
    private final double[][] coordinates;

    public TsplibTspProblem(String instanceName, double[][] coordinates) {
        this.instanceName = instanceName;
        this.coordinates = coordinates;
    }

    @Override
    public String name() {
        return "tsplib-" + instanceName;
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
    public List<String> violations(PermutationVector genotype) {
        if (genotype.size() != coordinates.length) {
            return List.of("Permutation size must equal TSPLIB city count " + coordinates.length);
        }
        return List.of();
    }

    public int cityCount() {
        return coordinates.length;
    }

    private static double distance(double[] a, double[] b) {
        double dx = a[0] - b[0];
        double dy = a[1] - b[1];
        return Math.rint(Math.sqrt(dx * dx + dy * dy));
    }
}
