/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.NormalizingFlowModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for lightweight normalizing-flow model.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class NormalizingFlowModelPlugin implements ModelPlugin<RealVector> {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "normalizing-flow";
    }

    /**
     * Returns a short human-readable component description.
     *
     * @return human-readable model description
     */
    @Override
    public String description() {
        return "Autoregressive tanh flow with covariance transport";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params model parameter map from YAML configuration
     * @return created component
     */
    @Override
    public NormalizingFlowModel create(Map<String, Object> params) {
        return new NormalizingFlowModel(
                Params.dbl(params, "jitter", 1e-9),
                Params.dbl(params, "learningRate", 0.7),
                Params.dbl(params, "maxSkew", 0.8)
        );
    }
}
