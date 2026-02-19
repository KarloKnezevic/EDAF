package com.knezevic.edaf.v3.core.api;

/**
 * Scalar fitness implementation used by single-objective algorithms.
 */
public record ScalarFitness(double value) implements Fitness {

    @Override
    public double[] objectives() {
        return new double[]{value};
    }

    @Override
    public double scalar() {
        return value;
    }

    @Override
    public boolean scalarNative() {
        return true;
    }

    @Override
    public String toString() {
        return "ScalarFitness{" + "value=" + value + '}';
    }
}
