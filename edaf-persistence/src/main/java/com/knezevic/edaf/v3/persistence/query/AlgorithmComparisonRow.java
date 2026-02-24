/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * Per-algorithm aggregate row for same-problem comparisons.
 */
public record AlgorithmComparisonRow(
        String algorithm,
        long totalRuns,
        long completedRuns,
        long successfulRuns,
        double successRate,
        Double meanBest,
        Double medianBest,
        Double stdBest,
        Double ert,
        Double sp1
) {
}
