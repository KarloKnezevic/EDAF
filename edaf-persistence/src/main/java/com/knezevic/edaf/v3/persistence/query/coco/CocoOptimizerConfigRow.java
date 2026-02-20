package com.knezevic.edaf.v3.persistence.query.coco;

/**
 * Optimizer config metadata for campaign details.
 */
public record CocoOptimizerConfigRow(
        long id,
        String campaignId,
        String optimizerId,
        String configPath,
        String algorithmType,
        String modelType,
        String representationType,
        String configYaml,
        String createdAt
) {
}
