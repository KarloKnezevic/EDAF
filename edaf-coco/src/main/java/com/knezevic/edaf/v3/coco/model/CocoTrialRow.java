package com.knezevic.edaf.v3.coco.model;

/**
 * Persisted COCO trial row.
 */
public record CocoTrialRow(
        String campaignId,
        String optimizerId,
        String runId,
        int functionId,
        int instanceId,
        int dimension,
        int repetition,
        long budgetEvaluations,
        Long evaluations,
        Double bestFitness,
        Long runtimeMillis,
        String status,
        boolean reachedTarget,
        Long evaluationsToTarget,
        double targetValue,
        String createdAt
) {
}
