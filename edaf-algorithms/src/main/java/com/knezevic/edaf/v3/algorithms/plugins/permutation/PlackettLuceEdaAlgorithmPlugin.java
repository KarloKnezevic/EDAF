/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.plugins.permutation;

import com.knezevic.edaf.v3.algorithms.permutation.PlackettLuceEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
  * Plugin for Plackett-Luce EDA driver.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class PlackettLuceEdaAlgorithmPlugin implements AlgorithmPlugin<PermutationVector> {

    /**
     * Returns plugin type identifier.
     *
      * @return plugin type identifier
     */
    @Override
    public String type() {
        return "plackett-luce-eda";
    }

    /**
     * Returns short component description.
     *
      * @return human-readable plugin description
     */
    @Override
    public String description() {
        return "Plackett-Luce EDA driver";
    }

    /**
     * Creates component instance from plugin dependencies and configuration parameters.
     *
     * @param dependencies algorithm dependency bundle resolved by framework
     * @param params algorithm parameter map from YAML configuration
      * @return algorithm instance
     */
    @Override
    public Algorithm<PermutationVector> create(AlgorithmDependencies<PermutationVector> dependencies, Map<String, Object> params) {
        return new PlackettLuceEdaAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
