/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query.coco;

/**
 * Optimizer config metadata for campaign details.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
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
