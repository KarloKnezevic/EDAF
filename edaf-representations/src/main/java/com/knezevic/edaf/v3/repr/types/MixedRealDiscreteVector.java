/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.types;

import java.util.Arrays;

/**
 * Mixed representation with real and discrete sections.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record MixedRealDiscreteVector(double[] realPart, int[] discretePart) {

    public MixedRealDiscreteVector {
        realPart = Arrays.copyOf(realPart, realPart.length);
        discretePart = Arrays.copyOf(discretePart, discretePart.length);
    }

    /**
     * Converts to string.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "MixedRealDiscreteVector{" +
                "realPart=" + Arrays.toString(realPart) +
                ", discretePart=" + Arrays.toString(discretePart) +
                '}';
    }
}
