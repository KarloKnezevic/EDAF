package com.knezevic.edaf.v3.persistence.query;

/**
 * Event row with type and JSON payload.
 */
public record EventRow(
        long id,
        String runId,
        String eventType,
        String payloadJson,
        String createdAt
) {
}
