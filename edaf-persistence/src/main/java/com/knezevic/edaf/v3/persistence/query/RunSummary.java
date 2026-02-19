package com.knezevic.edaf.v3.persistence.query;

/**
 * Lightweight run summary row.
 */
public record RunSummary(
        String runId,
        String algorithm,
        String model,
        String problem,
        String startTime,
        String endTime,
        Double bestFitness,
        Long runtimeMillis,
        String status
) {
}
