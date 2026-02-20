package com.knezevic.edaf.v3.problems.multiobjective;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.VectorFitness;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Arrays;

/**
 * DTLZ multi-objective benchmark suite adapter.
 */
public final class DtlzProblem implements Problem<RealVector> {

    private final int functionId;
    private final int objectives;
    private final double[] scalarWeights;

    public DtlzProblem(int functionId, int objectives, double[] scalarWeights) {
        if (objectives < 2) {
            throw new IllegalArgumentException("DTLZ objective count must be >= 2");
        }
        this.functionId = functionId;
        this.objectives = objectives;
        this.scalarWeights = scalarWeights == null || scalarWeights.length == 0
                ? defaultWeights(objectives)
                : Arrays.copyOf(scalarWeights, scalarWeights.length);
    }

    @Override
    public String name() {
        return "dtlz" + functionId + "-m" + objectives;
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    @Override
    public int objectiveCount() {
        return objectives;
    }

    @Override
    public Fitness evaluate(RealVector genotype) {
        double[] x = genotype.values();
        int n = x.length;
        int k = Math.max(1, n - objectives + 1);

        if (n < objectives) {
            double[] fallback = new double[objectives];
            Arrays.fill(fallback, 1.0e6);
            return new VectorFitness(fallback, scalarWeights);
        }

        return switch (functionId) {
            case 1 -> new VectorFitness(dtlz1(x, objectives, k), scalarWeights);
            case 2 -> new VectorFitness(dtlz2(x, objectives, k), scalarWeights);
            case 7 -> new VectorFitness(dtlz7(x, objectives, k), scalarWeights);
            default -> throw new IllegalArgumentException("Unsupported DTLZ functionId: " + functionId);
        };
    }

    private static double[] dtlz1(double[] x, int m, int k) {
        int n = x.length;
        double g = 0.0;
        for (int i = n - k; i < n; i++) {
            double z = x[i] - 0.5;
            g += z * z - Math.cos(20.0 * Math.PI * z);
        }
        g = 100.0 * (k + g);

        double[] f = new double[m];
        for (int i = 0; i < m; i++) {
            double value = 0.5 * (1.0 + g);
            for (int j = 0; j < m - i - 1; j++) {
                value *= x[j];
            }
            if (i > 0) {
                value *= (1.0 - x[m - i - 1]);
            }
            f[i] = value;
        }
        return f;
    }

    private static double[] dtlz2(double[] x, int m, int k) {
        int n = x.length;
        double g = 0.0;
        for (int i = n - k; i < n; i++) {
            double z = x[i] - 0.5;
            g += z * z;
        }

        double[] f = new double[m];
        for (int i = 0; i < m; i++) {
            double value = 1.0 + g;
            for (int j = 0; j < m - i - 1; j++) {
                value *= Math.cos(x[j] * Math.PI / 2.0);
            }
            if (i > 0) {
                value *= Math.sin(x[m - i - 1] * Math.PI / 2.0);
            }
            f[i] = value;
        }
        return f;
    }

    private static double[] dtlz7(double[] x, int m, int k) {
        int n = x.length;
        double[] f = new double[m];
        for (int i = 0; i < m - 1; i++) {
            f[i] = x[i];
        }

        double g = 0.0;
        for (int i = n - k; i < n; i++) {
            g += x[i];
        }
        g = 1.0 + 9.0 * g / k;

        double h = m;
        for (int i = 0; i < m - 1; i++) {
            double ratio = f[i] / (1.0 + g);
            h -= ratio * (1.0 + Math.sin(3.0 * Math.PI * f[i]));
        }
        f[m - 1] = (1.0 + g) * h;
        return f;
    }

    private static double[] defaultWeights(int objectives) {
        double[] weights = new double[objectives];
        Arrays.fill(weights, 1.0 / objectives);
        return weights;
    }
}
