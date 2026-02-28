/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.types;

import java.util.Arrays;

/**
 * Bounded integer vector genotype.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record IntVector(int[] values) {

    public IntVector {
        values = Arrays.copyOf(values, values.length);
    }

    /**
     * Executes length.
     *
     * @return the computed length
     */
    public int length() {
        return values.length;
    }

    /**
     * Converts to string.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
