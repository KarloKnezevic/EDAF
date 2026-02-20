package com.knezevic.edaf.v3.persistence.query.coco;

/**
 * Query object for COCO campaign filtering and pagination.
 */
public record CocoCampaignQuery(
        String q,
        String status,
        String suite,
        int page,
        int size,
        String sortBy,
        String sortDir
) {

    public static CocoCampaignQuery defaults() {
        return new CocoCampaignQuery(null, null, null, 0, 25, "created_at", "desc");
    }

    public int offset() {
        return Math.max(0, page) * Math.max(1, size);
    }
}
