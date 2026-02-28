/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.events;

import com.knezevic.edaf.v3.core.api.AdaptiveActionRecord;

import java.time.Instant;

/**
 * Event emitted whenever adaptive control modifies exploration/restart behavior.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record AdaptiveActionEvent(
        String runId,
        Instant timestamp,
        int iteration,
        AdaptiveActionRecord action
) implements RunEvent {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "adaptive_action";
    }
}
