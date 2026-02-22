package com.knezevic.edaf.v3.problems.discrete.disjunct;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Stateful evaluator for deviation
 * {@code delta(S) = |{x_j notin S : supp(x_j) subseteq union_{x_i in S} supp(x_i)}|}.
 *
 * <p>This class is intentionally mutable to reduce allocations during repeated
 * combination traversals. It is not thread-safe.</p>
 */
final class DisjunctDeviationEvaluator {

    private final DisjunctMatrix matrix;
    private final BitSet supportUnion;
    private final int[] membershipMarker;
    private int markerToken = 1;

    DisjunctDeviationEvaluator(DisjunctMatrix matrix) {
        this.matrix = matrix;
        this.supportUnion = new BitSet(matrix.rows());
        this.membershipMarker = new int[matrix.columns()];
    }

    int deviationForSubset(int[] subset) {
        int token = nextMarkerToken();
        supportUnion.clear();
        for (int columnIndex : subset) {
            if (columnIndex < 0 || columnIndex >= matrix.columns()) {
                throw new IllegalArgumentException("subset contains invalid column index " + columnIndex);
            }
            supportUnion.or(matrix.supportRef(columnIndex));
            membershipMarker[columnIndex] = token;
        }

        int deviation = 0;
        for (int column = 0; column < matrix.columns(); column++) {
            if (membershipMarker[column] == token) {
                continue;
            }
            if (isSubset(matrix.supportRef(column), supportUnion)) {
                deviation++;
            }
        }
        return deviation;
    }

    private int nextMarkerToken() {
        markerToken++;
        if (markerToken == Integer.MAX_VALUE) {
            Arrays.fill(membershipMarker, 0);
            markerToken = 1;
        }
        return markerToken;
    }

    private static boolean isSubset(BitSet candidate, BitSet superset) {
        for (int bit = candidate.nextSetBit(0); bit >= 0; bit = candidate.nextSetBit(bit + 1)) {
            if (!superset.get(bit)) {
                return false;
            }
        }
        return true;
    }
}
