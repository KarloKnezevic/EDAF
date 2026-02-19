package com.knezevic.edaf.v3.core.api;

import java.util.Arrays;

/**
 * Vector fitness used by multi-objective algorithms and diagnostics.
 */
public final class VectorFitness implements Fitness {

    private final double[] objectives;
    private final double scalar;

    /**
     * Builds a vector fitness that uses weighted sum as scalar projection.
     */
    public VectorFitness(double[] objectives, double[] weights) {
        this.objectives = Arrays.copyOf(objectives, objectives.length);
        double sum = 0.0;
        for (int i = 0; i < objectives.length; i++) {
            double w = i < weights.length ? weights[i] : 1.0;
            sum += objectives[i] * w;
        }
        this.scalar = sum;
    }

    /**
     * Builds a vector fitness with a precomputed scalar projection.
     */
    public VectorFitness(double[] objectives, double scalar) {
        this.objectives = Arrays.copyOf(objectives, objectives.length);
        this.scalar = scalar;
    }

    @Override
    public double[] objectives() {
        return Arrays.copyOf(objectives, objectives.length);
    }

    @Override
    public double scalar() {
        return scalar;
    }

    @Override
    public boolean scalarNative() {
        return false;
    }

    @Override
    public String toString() {
        return "VectorFitness{" + "objectives=" + Arrays.toString(objectives) + ", scalar=" + scalar + '}';
    }
}
