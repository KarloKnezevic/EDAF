package com.knezevic.edaf.v3.core.events;

import com.knezevic.edaf.v3.core.api.ModelDiagnostics;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted on each iteration with metrics and model diagnostics.
 */
public record IterationCompletedEvent(
        String runId,
        Instant timestamp,
        int iteration,
        long evaluations,
        double bestFitness,
        double meanFitness,
        double stdFitness,
        Map<String, Double> metrics,
        ModelDiagnostics diagnostics
) implements RunEvent {

    @Override
    public String type() {
        return "iteration_completed";
    }
}
