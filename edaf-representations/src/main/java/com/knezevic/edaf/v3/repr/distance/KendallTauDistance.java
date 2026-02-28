/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.distance;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Kendall tau inversion count distance for permutations.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class KendallTauDistance {

    private KendallTauDistance() {
        // utility class
    }

    /**
     * Computes Kendall tau distance (inversion count) between two permutations.
     *
     * @param a first permutation
     * @param b second permutation
     * @return the number of pairwise inversions between {@code a} and {@code b}
     */
    public static int between(PermutationVector a, PermutationVector b) {
        if (a.size() != b.size()) {
            throw new IllegalArgumentException("Permutations must have same size");
        }
        int n = a.size();
        int[] position = new int[n];
        for (int i = 0; i < n; i++) {
            position[b.order()[i]] = i;
        }
        int inversions = 0;
        int[] mapped = new int[n];
        for (int i = 0; i < n; i++) {
            mapped[i] = position[a.order()[i]];
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (mapped[i] > mapped[j]) {
                    inversions++;
                }
            }
        }
        return inversions;
    }
}
