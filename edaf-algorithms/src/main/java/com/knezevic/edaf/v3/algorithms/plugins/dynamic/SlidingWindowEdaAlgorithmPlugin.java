/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.plugins.dynamic;

import com.knezevic.edaf.v3.algorithms.dynamic.SlidingWindowEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;

import java.util.Map;

/**
 * Plugin for sliding-window dynamic EDA.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class SlidingWindowEdaAlgorithmPlugin implements AlgorithmPlugin<Object> {

    /**
     * Returns plugin type identifier.
     *
      * @return plugin type identifier
     */
    @Override
    public String type() {
        return "sliding-window-eda";
    }

    /**
     * Returns short component description.
     *
      * @return human-readable plugin description
     */
    @Override
    public String description() {
        return "Sliding-window dynamic EDA driver";
    }

    /**
     * Creates component instance from plugin dependencies and configuration parameters.
     *
     * @param dependencies algorithm dependency bundle resolved by framework
     * @param params algorithm parameter map from YAML configuration
      * @return algorithm instance
     */
    @Override
    public Algorithm<Object> create(AlgorithmDependencies<Object> dependencies, Map<String, Object> params) {
        return new SlidingWindowEdaAlgorithm<>(
                Params.dbl(params, "selectionRatio", 0.5),
                Params.dbl(params, "minSelectionRatio", 0.2),
                Params.dbl(params, "maxSelectionRatio", 0.9),
                Params.integer(params, "windowSize", 8),
                Params.dbl(params, "targetImprovement", 0.002),
                Params.dbl(params, "adjustmentStep", 0.02)
        );
    }
}
