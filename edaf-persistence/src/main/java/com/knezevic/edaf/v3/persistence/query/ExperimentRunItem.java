package com.knezevic.edaf.v3.persistence.query;

/**
 * One run row within a specific experiment.
 */
public record ExperimentRunItem(
        String runId,
        long seed,
        String status,
        String startTime,
        String endTime,
        Integer iterations,
        Long evaluations,
        Double bestFitness,
        Long runtimeMillis,
        String resumedFrom,
        String errorMessage
) {
}
