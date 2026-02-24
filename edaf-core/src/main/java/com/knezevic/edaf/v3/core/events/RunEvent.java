/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

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
