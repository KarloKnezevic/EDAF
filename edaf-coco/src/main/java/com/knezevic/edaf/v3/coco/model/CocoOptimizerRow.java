/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

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
