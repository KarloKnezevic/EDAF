package com.knezevic.edaf.v3.core.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Structured latent-knowledge payload emitted per iteration.
 *
 * <p>The payload separates scalar metrics from richer representation-specific insights so
 * sinks can both aggregate numeric signals and visualize detailed structures (heatmaps,
 * edge lists, trajectories).</p>
 */
public record LatentTelemetry(
        String representationFamily,
        Map<String, Double> metrics,
        Map<String, Object> insights,
        Map<String, Double> drift,
        Map<String, Double> diversity
) {

    public LatentTelemetry {
        representationFamily = representationFamily == null ? "unknown" : representationFamily;
        metrics = immutableCopy(metrics);
        insights = immutableObjectCopy(insights);
        drift = immutableCopy(drift);
        diversity = immutableCopy(diversity);
    }

    /**
     * Returns empty telemetry payload used when representation-specific analysis is unavailable.
     */
    public static LatentTelemetry empty() {
        return new LatentTelemetry("unknown", Map.of(), Map.of(), Map.of(), Map.of());
    }

    /**
     * Flattens all numeric maps into one map for compact CSV/DB storage.
     */
    public Map<String, Double> flattenedNumeric() {
        Map<String, Double> merged = new LinkedHashMap<>();
        merged.putAll(metrics);
        for (Map.Entry<String, Double> entry : drift.entrySet()) {
            merged.put("drift_" + entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Double> entry : diversity.entrySet()) {
            merged.put("diversity_" + entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(merged);
    }

    private static Map<String, Double> immutableCopy(Map<String, Double> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private static Map<String, Object> immutableObjectCopy(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
