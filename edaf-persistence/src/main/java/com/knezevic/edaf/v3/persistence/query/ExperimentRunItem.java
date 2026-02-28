/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * One run row within a specific experiment.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record ExperimentRunItem(
        String runId,
        long seed,
        String status,
        String startTime,
        String endTime,
        Integer iterations,
        Long evaluations,
        Double bestFitness,
        Long runtimeMillis,
        String resumedFrom,
        String errorMessage
) {
}
