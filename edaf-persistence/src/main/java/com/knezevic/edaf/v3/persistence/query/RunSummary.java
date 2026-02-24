/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * Lightweight run summary row.
 */
public record RunSummary(
        String runId,
        String algorithm,
        String model,
        String problem,
        String startTime,
        String endTime,
        Double bestFitness,
        Long runtimeMillis,
        String status
) {
}
