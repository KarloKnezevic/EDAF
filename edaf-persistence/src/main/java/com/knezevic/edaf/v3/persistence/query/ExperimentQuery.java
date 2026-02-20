package com.knezevic.edaf.v3.persistence.query;

/**
 * Query object for experiment-list filtering, sorting, and pagination.
 */
public record ExperimentQuery(
        String q,
        String algorithm,
        String model,
        String problem,
        String from,
        String to,
        int page,
        int size,
        String sortBy,
        String sortDir
) {

    public static ExperimentQuery defaults() {
        return new ExperimentQuery(null, null, null, null, null, null, 0, 25, "created_at", "desc");
    }

    public int offset() {
        return Math.max(0, page) * Math.max(1, size);
    }
}
