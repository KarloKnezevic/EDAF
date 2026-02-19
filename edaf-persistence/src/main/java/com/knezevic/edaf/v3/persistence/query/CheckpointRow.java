package com.knezevic.edaf.v3.persistence.query;

/**
 * Checkpoint persistence row for run detail view.
 */
public record CheckpointRow(
        long id,
        String runId,
        int iteration,
        String checkpointPath,
        String createdAt
) {
}
