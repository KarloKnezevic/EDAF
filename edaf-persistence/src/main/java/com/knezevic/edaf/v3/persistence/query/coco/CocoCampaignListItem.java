/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query.coco;

/**
 * Campaign list projection used by API/web pages.
 */
public record CocoCampaignListItem(
        String campaignId,
        String name,
        String suite,
        String status,
        String createdAt,
        String startedAt,
        String finishedAt,
        long trials,
        long reachedTargets,
        long optimizerCount
) {
}
