/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.events;

import com.knezevic.edaf.v3.core.api.AdaptiveActionRecord;

import java.time.Instant;

/**
 * Event emitted whenever adaptive control modifies exploration/restart behavior.
 */
public record AdaptiveActionEvent(
        String runId,
        Instant timestamp,
        int iteration,
        AdaptiveActionRecord action
) implements RunEvent {

    @Override
    public String type() {
        return "adaptive_action";
    }
}
