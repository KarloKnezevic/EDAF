package com.knezevic.edaf.v3.repr.distance;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Hamming distance utility for binary strings.
 */
public final class HammingDistance {

    private HammingDistance() {
        // utility class
    }

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
