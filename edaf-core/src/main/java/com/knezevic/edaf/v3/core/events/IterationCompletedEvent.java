package com.knezevic.edaf.v3.core.events;

import com.knezevic.edaf.v3.core.api.AdaptiveActionRecord;
import com.knezevic.edaf.v3.core.api.LatentTelemetry;
import com.knezevic.edaf.v3.core.api.ModelDiagnostics;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Event emitted on each iteration with metrics and model diagnostics.
 */
public record IterationCompletedEvent(
        String runId,
        Instant timestamp,
        int iteration,
        long evaluations,
        int populationSize,
        int eliteSize,
        double bestFitness,
        double meanFitness,
        double stdFitness,
        Map<String, Double> metrics,
        ModelDiagnostics diagnostics,
        LatentTelemetry latentTelemetry,
        List<AdaptiveActionRecord> adaptiveActions
) implements RunEvent {

    @Override
    public String type() {
        return "iteration_completed";
    }
}
