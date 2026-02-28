/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.coco.model;

/**
 * Aggregated COCO statistics per optimizer/dimension/target.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
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
