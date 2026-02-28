/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

/**
 * Scalar fitness implementation used by single-objective algorithms.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record ScalarFitness(double value) implements Fitness {

    /**
     * Returns scalar value wrapped as one-element objective vector.
     *
     * @return one-element objective vector
     */
    @Override
    public double[] objectives() {
        return new double[]{value};
    }

    /**
     * Returns scalar fitness value.
     *
     * @return scalar value
     */
    @Override
    public double scalar() {
        return value;
    }

    /**
     * Indicates that this implementation is scalar-native.
     *
     * @return true
     */
    @Override
    public boolean scalarNative() {
        return true;
    }

    /**
     * Returns debug string representation of this fitness value.
     *
     * @return debug string
     */
    @Override
    public String toString() {
        return "ScalarFitness{" + "value=" + value + '}';
    }
}
