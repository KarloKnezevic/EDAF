package com.knezevic.edaf.v3.core.events;

import java.time.Instant;

/**
 * Marker interface for all run lifecycle events.
 */
public interface RunEvent {

    /**
     * Run identifier.
     */
    String runId();

    /**
     * Event timestamp.
     */
    Instant timestamp();

    /**
     * Event type for structured logging.
     */
    String type();
}
