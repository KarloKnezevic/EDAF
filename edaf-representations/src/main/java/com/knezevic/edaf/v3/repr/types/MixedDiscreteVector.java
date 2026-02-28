/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.types;

import java.util.Arrays;

/**
 * Mixed discrete vector containing integer-encoded categorical attributes.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record MixedDiscreteVector(int[] encodedValues) {

    public MixedDiscreteVector {
        encodedValues = Arrays.copyOf(encodedValues, encodedValues.length);
    }

    /**
     * Executes length.
     *
     * @return the computed length
     */
    public int length() {
        return encodedValues.length;
    }

    /**
     * Converts to string.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return Arrays.toString(encodedValues);
    }
}
