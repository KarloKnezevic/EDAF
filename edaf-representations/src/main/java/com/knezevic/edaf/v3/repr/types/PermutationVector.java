/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.types;

import java.util.Arrays;

/**
 * Permutation genotype containing each index exactly once.
 */
public record PermutationVector(int[] order) {

    public PermutationVector {
        order = Arrays.copyOf(order, order.length);
    }

    public int size() {
        return order.length;
    }

    @Override
    public String toString() {
        return Arrays.toString(order);
    }
}
