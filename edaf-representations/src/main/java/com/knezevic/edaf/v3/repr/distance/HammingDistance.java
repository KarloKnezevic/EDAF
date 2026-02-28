/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.distance;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Hamming distance utility for binary strings.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class HammingDistance {

    private HammingDistance() {
        // utility class
    }

    /**
     * Computes Hamming distance between two bit strings.
     *
     * @param a first bit string
     * @param b second bit string
     * @return the number of positions where {@code a} and {@code b} differ
     */
    public static int between(BitString a, BitString b) {
        if (a.length() != b.length()) {
            throw new IllegalArgumentException("Bitstrings must have same length");
        }
        int distance = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.genes()[i] != b.genes()[i]) {
                distance++;
            }
        }
        return distance;
    }
}
