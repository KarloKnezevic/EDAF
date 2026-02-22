package com.knezevic.edaf.v3.core.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One adaptive action triggered by latent-knowledge signals.
 */
public record AdaptiveActionRecord(
        String trigger,
        String actionType,
        String reason,
        Map<String, Object> details
) {

    public AdaptiveActionRecord {
        trigger = trigger == null ? "unknown" : trigger;
        actionType = actionType == null ? "unknown" : actionType;
        reason = reason == null ? "" : reason;
        details = details == null || details.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(details));
    }
}
