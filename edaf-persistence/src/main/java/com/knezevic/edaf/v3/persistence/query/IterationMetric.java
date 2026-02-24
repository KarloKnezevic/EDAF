/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * Iteration metric row used by reports and web charts.
 */
public record IterationMetric(
        int iteration,
        long evaluations,
        double bestFitness,
        double meanFitness,
        double stdFitness,
        String metricsJson,
        String diagnosticsJson,
        String createdAt
) {
}
