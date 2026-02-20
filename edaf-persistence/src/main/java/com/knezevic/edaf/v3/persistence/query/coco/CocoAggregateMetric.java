package com.knezevic.edaf.v3.persistence.query.coco;

/**
 * Aggregate metric row persisted per optimizer/dimension.
 */
public record CocoAggregateMetric(
        long id,
        String campaignId,
        String optimizerId,
        int dimension,
        double targetValue,
        Double meanEvaluationsToTarget,
        double successRate,
        Double medianBestFitness,
        String comparedReferenceOptimizer,
        Double referenceErt,
        Double edafErt,
        Double ertRatio,
        String createdAt
) {
}
