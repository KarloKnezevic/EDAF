package com.knezevic.edaf.v3.persistence.query;

/**
 * Rich run + experiment view used by run detail API and dashboard.
 */
public record RunDetail(
        String runId,
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
        String status,
        long seed,
        String startTime,
        String endTime,
        Integer iterations,
        Long evaluations,
        Double bestFitness,
        String bestSummary,
        Long runtimeMillis,
        String artifactsJson,
        String resumedFrom,
        String errorMessage,
        String configYaml,
        String configJson,
        String experimentCreatedAt
) {
}
