/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.types;

import java.util.Arrays;

/**
 * Permutation genotype containing each index exactly once.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record PermutationVector(int[] order) {

    public PermutationVector {
        order = Arrays.copyOf(order, order.length);
    }

    /**
     * Executes size.
     *
     * @return the number of elements
     */
    public int size() {
        return order.length;
    }

    /**
     * Converts to string.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return Arrays.toString(order);
    }
}
