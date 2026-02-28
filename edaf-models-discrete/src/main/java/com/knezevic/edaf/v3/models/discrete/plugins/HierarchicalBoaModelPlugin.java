/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.discrete.HierarchicalBoaModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for hierarchical BOA-style sparse Bayesian-network model.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class HierarchicalBoaModelPlugin implements ModelPlugin<BitString> {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "hboa-network";
    }

    /**
     * Returns a short human-readable component description.
     *
     * @return human-readable model description
     */
    @Override
    public String description() {
        return "Hierarchical BOA sparse Bayesian-network model";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params model parameter map from YAML configuration
     * @return created component
     */
    @Override
    public HierarchicalBoaModel create(Map<String, Object> params) {
        return new HierarchicalBoaModel(
                Params.dbl(params, "smoothing", 0.5),
                Params.dbl(params, "minMutualInformation", 1e-4),
                Params.dbl(params, "learningRate", 0.8)
        );
    }
}
