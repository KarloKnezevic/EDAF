package com.knezevic.edaf.v3.problems.discrete.disjunct;

/**
 * Evaluation policy for objective computation during optimization.
 */
public record DisjunctEvaluationConfig(
        DisjunctEvaluationMode mode,
        long maxExactSubsets,
        long sampleSize,
        long samplingSeed
) {

    public DisjunctEvaluationConfig {
        if (mode == null) {
            throw new IllegalArgumentException("mode must not be null");
        }
        if (maxExactSubsets < 1L) {
            throw new IllegalArgumentException("maxExactSubsets must be >= 1");
        }
        if (sampleSize < 1L) {
            throw new IllegalArgumentException("sampleSize must be >= 1");
        }
    }

    /**
     * Default policy:
     * exact up to 1000 subsets, sampled above with 512 sampled subsets.
     */
    public static DisjunctEvaluationConfig defaults() {
        return new DisjunctEvaluationConfig(
                DisjunctEvaluationMode.AUTO,
                1_000L,
                512L,
                7_331L
        );
    }

    /**
     * Resolves concrete mode from total subset count.
     */
    public DisjunctEvaluationMode resolve(long totalSubsets) {
        if (mode != DisjunctEvaluationMode.AUTO) {
            return mode;
        }
        return totalSubsets <= maxExactSubsets
                ? DisjunctEvaluationMode.EXACT
                : DisjunctEvaluationMode.SAMPLED;
    }
}
