/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * Checkpoint persistence row for run detail view.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record CheckpointRow(
        long id,
        String runId,
        int iteration,
        String checkpointPath,
        String createdAt
) {
}
