package com.knezevic.edaf.v3.problems.discrete.disjunct;

/**
 * Objective evaluation mode for DM/RM/ADM fitness computation.
 */
public enum DisjunctEvaluationMode {
    /**
     * Always use exhaustive exact combinatorial evaluation.
     */
    EXACT,
    /**
     * Always use sampled estimator.
     */
    SAMPLED,
    /**
     * Use exact below threshold and sampled above threshold.
     */
    AUTO
}
