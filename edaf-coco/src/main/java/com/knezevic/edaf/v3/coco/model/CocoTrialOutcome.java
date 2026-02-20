package com.knezevic.edaf.v3.coco.model;

/**
 * Trial-level COCO outcome persisted after one EDAF run.
 */
public record CocoTrialOutcome(
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
        double targetValue
) {
}
