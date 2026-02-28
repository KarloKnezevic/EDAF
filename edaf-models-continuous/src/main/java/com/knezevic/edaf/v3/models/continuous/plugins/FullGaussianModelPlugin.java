/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.FullGaussianModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for full-covariance Gaussian model.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class FullGaussianModelPlugin implements ModelPlugin<RealVector> {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "gaussian-full";
    }

    /**
     * Returns a short human-readable component description.
     *
     * @return human-readable model description
     */
    @Override
    public String description() {
        return "Full covariance Gaussian density estimator";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params model parameter map from YAML configuration
     * @return created component
     */
    @Override
    public FullGaussianModel create(Map<String, Object> params) {
        return new FullGaussianModel(
                Params.dbl(params, "jitter", 1e-9),
                Params.dbl(params, "learningRate", 1.0),
                Params.dbl(params, "shrinkage", 0.0)
        );
    }
}
