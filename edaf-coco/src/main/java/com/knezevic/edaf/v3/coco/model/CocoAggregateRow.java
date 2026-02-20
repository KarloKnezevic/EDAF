package com.knezevic.edaf.v3.coco.model;

/**
 * Aggregated COCO statistics per optimizer/dimension/target.
 */
public record CocoAggregateRow(
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
        Double ertRatio
) {
}
