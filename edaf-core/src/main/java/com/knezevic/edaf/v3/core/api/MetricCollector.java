/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import java.util.Map;

/**
 * Hook for collecting iteration metrics and diagnostics.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface MetricCollector<G> {

    /**
     * Returns collector identifier used for grouped diagnostics.
     *
     * @return metric collector identifier
     */
    String name();

    /**
     * Collects numeric metrics from the current algorithm state.
     *
     * @param state current algorithm state snapshot
     * @return key-value metric map
     */
    Map<String, Double> collect(AlgorithmState<G> state);
}
