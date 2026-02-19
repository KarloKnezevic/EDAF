package com.knezevic.edaf.v3.repr.types;

import java.util.Arrays;

/**
 * Bounded integer vector genotype.
 */
public record IntVector(int[] values) {

    public IntVector {
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
