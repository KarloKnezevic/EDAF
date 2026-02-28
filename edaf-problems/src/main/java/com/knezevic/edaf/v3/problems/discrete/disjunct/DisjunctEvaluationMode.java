/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.discrete.disjunct;

/**
 * Objective evaluation mode for DM/RM/ADM fitness computation.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
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
