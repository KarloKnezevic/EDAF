package com.knezevic.edaf.v3.persistence.query;

/**
 * Experiment-level metadata with run counters for aggregated analysis views.
 */
public record ExperimentDetail(
        String experimentId,
        String configHash,
        String schemaVersion,
        String runName,
        String algorithmType,
        String modelType,
        String problemType,
        String representationType,
        String selectionType,
        String replacementType,
        String stoppingType,
        Integer maxIterations,
        String configYaml,
        String configJson,
        String createdAt,
        long totalRuns,
        long completedRuns,
        long failedRuns,
        long runningRuns
) {
}
