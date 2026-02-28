/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.plugins.mo;

import com.knezevic.edaf.v3.algorithms.mo.MoEdaSkeletonAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;

import java.util.Map;

/**
 * Plugin for multi-objective EDA skeleton driver.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MoEdaSkeletonAlgorithmPlugin implements AlgorithmPlugin<Object> {

    /**
     * Returns plugin type identifier.
     *
      * @return plugin type identifier
     */
    @Override
    public String type() {
        return "mo-eda-skeleton";
    }

    /**
     * Returns short component description.
     *
      * @return human-readable plugin description
     */
    @Override
    public String description() {
        return "Baseline multi-objective EDA driver";
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
        return new MoEdaSkeletonAlgorithm<>(Params.dbl(params, "selectionRatio", 0.5));
    }
}
