/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import java.util.Arrays;

/**
 * Vector fitness used by multi-objective algorithms and diagnostics.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class VectorFitness implements Fitness {

    private final double[] objectives;
    private final double scalar;

    /**
     * Builds a vector fitness that uses weighted sum as scalar projection.
     *
     * @param objectives objective vector
     * @param weights projection weights
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
     *
     * @param objectives objective vector
     * @param scalar scalar projection
     */
    public VectorFitness(double[] objectives, double scalar) {
        this.objectives = Arrays.copyOf(objectives, objectives.length);
        this.scalar = scalar;
    }

    /**
     * Returns a defensive copy of objective vector.
     *
     * @return objective values
     */
    @Override
    public double[] objectives() {
        return Arrays.copyOf(objectives, objectives.length);
    }

    /**
     * Returns scalar projection used by single-objective consumers.
     *
     * @return scalar projection
     */
    @Override
    public double scalar() {
        return scalar;
    }

    /**
     * Indicates that this implementation is vector-native.
     *
     * @return false
     */
    @Override
    public boolean scalarNative() {
        return false;
    }

    /**
     * Returns debug string representation of objective vector and scalar projection.
     *
     * @return debug string
     */
    @Override
    public String toString() {
        return "VectorFitness{" + "objectives=" + Arrays.toString(objectives) + ", scalar=" + scalar + '}';
    }
}
