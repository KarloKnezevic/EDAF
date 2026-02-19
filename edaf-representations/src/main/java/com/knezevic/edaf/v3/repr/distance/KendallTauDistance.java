package com.knezevic.edaf.v3.repr.distance;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Kendall tau inversion count distance for permutations.
 */
public final class KendallTauDistance {

    private KendallTauDistance() {
        // utility class
    }

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
