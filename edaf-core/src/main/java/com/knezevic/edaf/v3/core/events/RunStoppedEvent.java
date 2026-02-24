/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.events;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when a run is stopped cooperatively by user/API request.
 */
public record RunStoppedEvent(
        String runId,
        Instant timestamp,
        String algorithm,
        String model,
        String problem,
        long masterSeed,
        int iterations,
        long evaluations,
        long runtimeMillis,
        double bestFitness,
        String bestSummary,
        String bestGenotype,
        Map<String, String> artifacts,
        String reason,
        String resumedFrom
) implements RunEvent {

    @Override
    public String type() {
        return "run_stopped";
    }
}
