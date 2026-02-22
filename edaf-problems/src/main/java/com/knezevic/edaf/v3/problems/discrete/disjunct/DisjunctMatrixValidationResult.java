package com.knezevic.edaf.v3.problems.discrete.disjunct;

import java.util.Arrays;

/**
 * Structured validation outcome for DM/RM/ADM definitions.
 *
 * <p>When {@code exact=true}, {@code valid} is mathematically exact.
 * In sampled mode, {@code valid=true} means no violating subset was observed and
 * the returned confidence bound quantifies residual uncertainty.</p>
 */
public record DisjunctMatrixValidationResult(
        String definition,
        DisjunctMatrixValidationMode mode,
        boolean valid,
        boolean exact,
        long evaluatedSubsets,
        long totalSubsets,
        long violatingSubsets,
        double estimatedViolationRate,
        double confidenceLevel,
        double errorBound,
        double violationRateUpperBound,
        int witnessDeviation,
        int[] witnessSubset,
        String message
) {

    public DisjunctMatrixValidationResult {
        witnessSubset = witnessSubset == null ? new int[0] : Arrays.copyOf(witnessSubset, witnessSubset.length);
    }

    /**
     * Returns true only when a concrete violating subset witness was discovered.
     */
    public boolean hasWitness() {
        return witnessSubset.length > 0;
    }
}
