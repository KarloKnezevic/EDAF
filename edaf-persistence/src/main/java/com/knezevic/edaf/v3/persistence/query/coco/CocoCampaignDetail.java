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
