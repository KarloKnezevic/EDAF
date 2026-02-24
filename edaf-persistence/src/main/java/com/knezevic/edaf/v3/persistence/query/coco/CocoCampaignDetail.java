/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query.coco;

/**
 * Full campaign metadata row.
 */
public record CocoCampaignDetail(
        String campaignId,
        String name,
        String suite,
        String dimensionsJson,
        String instancesJson,
        String functionsJson,
        String status,
        String createdAt,
        String startedAt,
        String finishedAt,
        String notes
) {
}
