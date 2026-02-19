package com.knezevic.edaf.v3.core.metrics;

import com.knezevic.edaf.v3.core.api.AlgorithmState;
import com.knezevic.edaf.v3.core.api.MetricCollector;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default metric collector publishing core scalar run indicators.
 */
public final class DefaultMetricCollector<G> implements MetricCollector<G> {

    @Override
    public String name() {
        return "default";
    }

    @Override
    public Map<String, Double> collect(AlgorithmState<G> state) {
        Map<String, Double> values = new LinkedHashMap<>();
        values.put("best", PopulationMetrics.best(state.population()));
        values.put("mean", PopulationMetrics.mean(state.population()));
        values.put("std", PopulationMetrics.std(state.population()));
        values.put("diversity", PopulationMetrics.diversity(state.population()));
        values.put("entropy", PopulationMetrics.entropy(state.population()));
        values.put("evaluations", (double) state.evaluations());
        values.put("iteration", (double) state.iteration());
        return values;
    }
}
