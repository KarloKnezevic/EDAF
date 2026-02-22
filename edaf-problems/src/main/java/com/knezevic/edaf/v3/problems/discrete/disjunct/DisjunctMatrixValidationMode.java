package com.knezevic.edaf.v3.problems.discrete.disjunct;

/**
 * Validation execution strategy.
 */
public enum DisjunctMatrixValidationMode {
    /**
     * Exhaustive combinatorial enumeration of all t-subsets.
     */
    EXACT,
    /**
     * Monte Carlo sampling of t-subsets with concentration bound metadata.
     */
    SAMPLED
}
