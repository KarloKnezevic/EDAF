/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query.coco;

/**
 * Query object for COCO campaign filtering and pagination.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
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

    /**
     * Executes defaults.
     *
     * @return the defaults
     */
    public static CocoCampaignQuery defaults() {
        return new CocoCampaignQuery(null, null, null, 0, 25, "created_at", "desc");
    }

    /**
     * Executes offset.
     *
     * @return the computed offset
     */
    public int offset() {
        return Math.max(0, page) * Math.max(1, size);
    }
}
