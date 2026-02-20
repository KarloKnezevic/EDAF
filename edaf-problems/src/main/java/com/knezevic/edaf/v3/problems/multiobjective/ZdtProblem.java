package com.knezevic.edaf.v3.problems.multiobjective;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.VectorFitness;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Arrays;

/**
 * ZDT multi-objective benchmark suite adapter.
 */
public final class ZdtProblem implements Problem<RealVector> {

    private final int functionId;
    private final double[] scalarWeights;

    public ZdtProblem(int functionId, double[] scalarWeights) {
        this.functionId = functionId;
        this.scalarWeights = scalarWeights == null || scalarWeights.length == 0
                ? new double[]{0.5, 0.5}
                : Arrays.copyOf(scalarWeights, scalarWeights.length);
    }

    @Override
    public String name() {
        return "zdt" + functionId;
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    @Override
    public int objectiveCount() {
        return 2;
    }

    @Override
    public Fitness evaluate(RealVector genotype) {
        double[] x = genotype.values();
        if (x.length < 2) {
            double fallback = 1.0e6;
            return new VectorFitness(new double[]{fallback, fallback}, scalarWeights);
        }

        double f1;
        double g;
        double f2;

        switch (functionId) {
            case 1 -> {
                f1 = x[0];
                g = 1.0 + 9.0 * averageTail(x);
                f2 = g * (1.0 - Math.sqrt(safeRatio(f1, g)));
            }
            case 2 -> {
                f1 = x[0];
                g = 1.0 + 9.0 * averageTail(x);
                double r = safeRatio(f1, g);
                f2 = g * (1.0 - r * r);
            }
            case 3 -> {
                f1 = x[0];
                g = 1.0 + 9.0 * averageTail(x);
                double r = safeRatio(f1, g);
                f2 = g * (1.0 - Math.sqrt(r) - r * Math.sin(10.0 * Math.PI * f1));
            }
            case 4 -> {
                f1 = x[0];
                double sum = 0.0;
                for (int i = 1; i < x.length; i++) {
                    double xi = x[i];
                    sum += xi * xi - 10.0 * Math.cos(4.0 * Math.PI * xi);
                }
                g = 1.0 + 10.0 * (x.length - 1) + sum;
                f2 = g * (1.0 - Math.sqrt(safeRatio(f1, g)));
            }
            case 6 -> {
                f1 = 1.0 - Math.exp(-4.0 * x[0]) * Math.pow(Math.sin(6.0 * Math.PI * x[0]), 6.0);
                g = 1.0 + 9.0 * Math.pow(averageTail(x), 0.25);
                f2 = g * (1.0 - Math.pow(safeRatio(f1, g), 2.0));
            }
            default -> throw new IllegalArgumentException("Unsupported ZDT functionId: " + functionId);
        }

        return new VectorFitness(new double[]{f1, f2}, scalarWeights);
    }

    private static double averageTail(double[] x) {
        if (x.length <= 1) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = 1; i < x.length; i++) {
            sum += x[i];
        }
        return sum / (x.length - 1.0);
    }

    private static double safeRatio(double numerator, double denominator) {
        if (Math.abs(denominator) < 1e-12) {
            return 0.0;
        }
        return numerator / denominator;
    }
}
