/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One adaptive action triggered by latent-knowledge signals.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record AdaptiveActionRecord(
        String trigger,
        String actionType,
        String reason,
        Map<String, Object> details
) {

    /**
     * Creates immutable adaptive action record with normalized default values.
     *
     * @param trigger trigger identifier
     * @param actionType adaptive action identifier
     * @param reason human-readable explanation
     * @param details additional structured action metadata
     */
    public AdaptiveActionRecord {
        trigger = trigger == null ? "unknown" : trigger;
        actionType = actionType == null ? "unknown" : actionType;
        reason = reason == null ? "" : reason;
        details = details == null || details.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(details));
    }
}
