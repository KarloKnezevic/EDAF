/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

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
