package com.knezevic.edaf.v3.persistence.query;

/**
 * One row in experiment listing.
 */
public record ExperimentListItem(
        String experimentId,
        String runName,
        String algorithmType,
        String modelType,
        String problemType,
        String representationType,
        String configHash,
        String createdAt,
        String latestRunTime,
        Long totalRuns,
        Long completedRuns,
        Long failedRuns,
        Double bestFitness
) {
}
