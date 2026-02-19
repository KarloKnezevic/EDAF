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
