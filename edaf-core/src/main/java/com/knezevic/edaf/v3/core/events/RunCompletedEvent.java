package com.knezevic.edaf.v3.core.events;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when a run terminates.
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

    @Override
    public String type() {
        return "run_completed";
    }
}
