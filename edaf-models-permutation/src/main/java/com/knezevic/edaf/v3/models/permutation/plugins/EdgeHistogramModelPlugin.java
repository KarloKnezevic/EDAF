/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.models.permutation.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.permutation.EdgeHistogramModel;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
 * Plugin factory for edge histogram model.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class EdgeHistogramModelPlugin implements ModelPlugin<PermutationVector> {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "ehm";
    }

    /**
     * Returns a short human-readable component description.
     *
     * @return human-readable model description
     */
    @Override
    public String description() {
        return "Edge Histogram Model for permutation EDAs";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params model parameter map from YAML configuration
     * @return created component
     */
    @Override
    public EdgeHistogramModel create(Map<String, Object> params) {
        return new EdgeHistogramModel(Params.dbl(params, "epsilon", 1e-6));
    }
}
