/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.plugins.discrete;

import com.knezevic.edaf.v3.algorithms.discrete.MimicAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
  * Plugin for MIMIC driver.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MimicAlgorithmPlugin implements AlgorithmPlugin<BitString> {

    /**
     * Returns plugin type identifier.
     *
      * @return plugin type identifier
     */
    @Override
    public String type() {
        return "mimic";
    }

    /**
     * Returns short component description.
     *
      * @return human-readable plugin description
     */
    @Override
    public String description() {
        return "MIMIC driver";
    }

    /**
     * Creates component instance from plugin dependencies and configuration parameters.
     *
     * @param dependencies algorithm dependency bundle resolved by framework
     * @param params algorithm parameter map from YAML configuration
      * @return algorithm instance
     */
    @Override
    public Algorithm<BitString> create(AlgorithmDependencies<BitString> dependencies, Map<String, Object> params) {
        return new MimicAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
