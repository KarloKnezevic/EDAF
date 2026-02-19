package com.knezevic.edaf.v3.repr.types;

import java.util.Arrays;

/**
 * Mixed discrete vector containing integer-encoded categorical attributes.
 */
public record MixedDiscreteVector(int[] encodedValues) {

    public MixedDiscreteVector {
        encodedValues = Arrays.copyOf(encodedValues, encodedValues.length);
    }

    public int length() {
        return encodedValues.length;
    }

    @Override
    public String toString() {
        return Arrays.toString(encodedValues);
    }
}
