/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.coco.model;

import java.util.List;

/**
 * Rich campaign snapshot used by HTML report generation.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record CocoCampaignSnapshot(
        String campaignId,
        String name,
        String suite,
        String status,
        String createdAt,
        String startedAt,
        String finishedAt,
        String dimensionsJson,
        String instancesJson,
        String functionsJson,
        String notes,
        List<CocoOptimizerRow> optimizers,
        List<CocoAggregateRow> aggregates,
        List<CocoTrialRow> trials
) {
}
