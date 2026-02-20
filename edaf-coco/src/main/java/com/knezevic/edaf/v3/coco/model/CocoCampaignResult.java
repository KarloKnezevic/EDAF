package com.knezevic.edaf.v3.coco.model;

import java.nio.file.Path;
import java.util.Map;

/**
 * Result of a completed COCO campaign execution.
 */
public record CocoCampaignResult(
        String campaignId,
        int executedTrials,
        int successfulTrials,
        Path htmlReport,
        Map<String, String> artifacts
) {
}
