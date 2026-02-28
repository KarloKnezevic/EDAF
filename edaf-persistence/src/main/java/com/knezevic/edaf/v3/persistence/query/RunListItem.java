/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * One row in filtered run listing.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record RunListItem(
        String runId,
        String experimentId,
        String runName,
        String algorithmType,
        String modelType,
        String problemType,
        String representationType,
        String status,
        String startTime,
        String endTime,
        Integer iterations,
        Long evaluations,
        Double bestFitness,
        Long runtimeMillis,
        String configHash
) {
}
