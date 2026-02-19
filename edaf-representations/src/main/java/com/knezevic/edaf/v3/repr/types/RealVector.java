package com.knezevic.edaf.v3.repr.types;

import java.util.Arrays;

/**
 * Real-valued vector genotype.
 */
public record RealVector(double[] values) {

    public RealVector {
        values = Arrays.copyOf(values, values.length);
    }

    public int length() {
        return values.length;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
