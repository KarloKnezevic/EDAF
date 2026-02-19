package com.knezevic.edaf.v3.repr.types;

import java.util.Arrays;

/**
 * Mixed representation with real and discrete sections.
 */
public record MixedRealDiscreteVector(double[] realPart, int[] discretePart) {

    public MixedRealDiscreteVector {
        realPart = Arrays.copyOf(realPart, realPart.length);
        discretePart = Arrays.copyOf(discretePart, discretePart.length);
    }

    @Override
    public String toString() {
        return "MixedRealDiscreteVector{" +
                "realPart=" + Arrays.toString(realPart) +
                ", discretePart=" + Arrays.toString(discretePart) +
                '}';
    }
}
