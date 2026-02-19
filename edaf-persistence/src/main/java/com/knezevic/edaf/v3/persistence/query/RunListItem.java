package com.knezevic.edaf.v3.persistence.query;

/**
 * One row in filtered run listing.
 */
public record RunListItem(
        String runId,
        String experimentId,
        String runName,
        String algorithmType,
        String modelType,
        String problemType,
        String representationType,
        String status,
        String startTime,
        String endTime,
        Integer iterations,
        Long evaluations,
        Double bestFitness,
        Long runtimeMillis,
        String configHash
) {
}
