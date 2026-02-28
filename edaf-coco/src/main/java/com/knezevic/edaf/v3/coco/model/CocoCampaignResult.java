/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.coco.model;

import java.nio.file.Path;
import java.util.Map;

/**
 * Result of a completed COCO campaign execution.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record CocoCampaignResult(
        String campaignId,
        int executedTrials,
        int successfulTrials,
        Path htmlReport,
        Map<String, String> artifacts
) {
}
