package com.knezevic.edaf.v3.core.api;

import java.util.Map;

/**
 * Hook for collecting iteration metrics and diagnostics.
 */
public interface MetricCollector<G> {

    /**
     * Collector identifier used for grouped diagnostics.
     */
    String name();

    /**
     * Returns key-value metrics for current state.
     */
    Map<String, Double> collect(AlgorithmState<G> state);
}
