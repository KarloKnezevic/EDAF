/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * Result of requesting cooperative stop for one run or experiment.
 */
public record StopRequestResult(
        String scope,
        String targetId,
        boolean found,
        boolean accepted,
        int affectedRuns,
        String message
) {
}
