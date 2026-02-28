/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.events;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when a run terminates.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record RunCompletedEvent(
        String runId,
        Instant timestamp,
        int iterations,
        long evaluations,
        long runtimeMillis,
        double bestFitness,
        String bestSummary,
        String bestGenotype,
        Map<String, String> artifacts
) implements RunEvent {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "run_completed";
    }
}
