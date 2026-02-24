/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.events;

import java.time.Instant;

/**
 * Event emitted when a run starts.
 */
public record RunStartedEvent(
        String runId,
        Instant timestamp,
        String algorithm,
        String model,
        String problem,
        long masterSeed
) implements RunEvent {

    @Override
    public String type() {
        return "run_started";
    }
}
