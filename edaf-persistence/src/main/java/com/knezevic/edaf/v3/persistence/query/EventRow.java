/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

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
