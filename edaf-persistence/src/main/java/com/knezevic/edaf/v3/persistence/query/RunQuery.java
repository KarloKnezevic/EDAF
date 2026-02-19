package com.knezevic.edaf.v3.persistence.query;

/**
 * Query object for run-list filtering, sorting, and pagination.
 */
public record RunQuery(
        String q,
        String algorithm,
        String model,
        String problem,
        String status,
        String from,
        String to,
        Double minBest,
        Double maxBest,
        int page,
        int size,
        String sortBy,
        String sortDir
) {

    public static RunQuery defaults() {
        return new RunQuery(null, null, null, null, null, null, null, null, null, 0, 25, "start_time", "desc");
    }

    public int offset() {
        return Math.max(0, page) * Math.max(1, size);
    }
}
