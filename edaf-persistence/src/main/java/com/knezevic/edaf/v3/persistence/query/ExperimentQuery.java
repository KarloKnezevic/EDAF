/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * Query object for experiment-list filtering, sorting, and pagination.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record ExperimentQuery(
        String q,
        String algorithm,
        String model,
        String problem,
        String status,
        String from,
        String to,
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
    public static ExperimentQuery defaults() {
        return new ExperimentQuery(null, null, null, null, null, null, null, 0, 25, "latest_run_time", "desc");
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
