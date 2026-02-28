/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.metrics;

import com.knezevic.edaf.v3.core.api.AlgorithmState;
import com.knezevic.edaf.v3.core.api.MetricCollector;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default run metric collector.
 *
 * <p>Emits a stable minimal set of key performance indicators expected by CLI,
 * persistence sinks, reporting and web dashboards. This collector is intentionally
 * lightweight so it can run every iteration without measurable overhead.</p>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class DefaultMetricCollector<G> implements MetricCollector<G> {

    /**
     * Returns component name identifier.
     *
     * @return component name
     */
    @Override
    public String name() {
        return "default";
    }

    /**
     * Collects metric values from state.
     *
     * @param state algorithm state
     * @return metric map
     */
    @Override
    public Map<String, Double> collect(AlgorithmState<G> state) {
        Map<String, Double> values = new LinkedHashMap<>();
        values.put("best", PopulationMetrics.best(state.population()));
        values.put("mean", PopulationMetrics.mean(state.population()));
        values.put("std", PopulationMetrics.std(state.population()));
        values.put("diversity", PopulationMetrics.diversity(state.population()));
        values.put("entropy", PopulationMetrics.entropy(state.population()));

        double[] objectives = state.best().fitness().objectives();
        values.put("objective_count", (double) objectives.length);
        for (int i = 0; i < objectives.length; i++) {
            values.put("best_obj_" + i, objectives[i]);
        }

        values.put("evaluations", (double) state.evaluations());
        values.put("iteration", (double) state.iteration());
        return values;
    }
}
