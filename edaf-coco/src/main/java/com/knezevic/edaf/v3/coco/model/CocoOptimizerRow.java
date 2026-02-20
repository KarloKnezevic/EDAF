package com.knezevic.edaf.v3.coco.model;

/**
 * Optimizer entry registered for one COCO campaign.
 */
public record CocoOptimizerRow(
        String campaignId,
        String optimizerId,
        String configPath,
        String algorithmType,
        String modelType,
        String representationType,
        String createdAt
) {
}
