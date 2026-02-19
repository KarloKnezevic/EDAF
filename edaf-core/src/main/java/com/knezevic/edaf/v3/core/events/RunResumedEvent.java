package com.knezevic.edaf.v3.core.events;

import java.time.Instant;

/**
 * Event emitted when a run is resumed from checkpoint.
 */
public record RunResumedEvent(
        String runId,
        Instant timestamp,
        int iteration,
        String checkpointPath
) implements RunEvent {

    @Override
    public String type() {
        return "run_resumed";
    }
}
