/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.GmmModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for diagonal-covariance Gaussian Mixture model.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class GmmModelPlugin implements ModelPlugin<RealVector> {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "gmm";
    }

    /**
     * Returns a short human-readable component description.
     *
     * @return human-readable model description
     */
    @Override
    public String description() {
        return "Gaussian mixture model with EM updates";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params model parameter map from YAML configuration
     * @return created component
     */
    @Override
    public GmmModel create(Map<String, Object> params) {
        return new GmmModel(
                Params.integer(params, "components", 3),
                Params.integer(params, "emIterations", 20),
                Params.dbl(params, "minVariance", 1.0e-8)
        );
    }
}
