package com.knezevic.edaf.v3.persistence.query.coco;

/**
 * Trial-level metric row for one campaign run.
 */
public record CocoTrialMetric(
        long id,
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
