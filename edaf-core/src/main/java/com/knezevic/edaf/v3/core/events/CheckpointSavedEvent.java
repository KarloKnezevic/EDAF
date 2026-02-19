package com.knezevic.edaf.v3.core.events;

import java.time.Instant;

/**
 * Event emitted when a checkpoint is persisted.
 */
public record CheckpointSavedEvent(
        String runId,
        Instant timestamp,
        int iteration,
        String checkpointPath
) implements RunEvent {

    @Override
    public String type() {
        return "checkpoint_saved";
    }
}
