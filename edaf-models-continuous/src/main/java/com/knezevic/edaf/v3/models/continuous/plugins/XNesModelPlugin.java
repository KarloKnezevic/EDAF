/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.XNesModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for xNES full-covariance model.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class XNesModelPlugin implements ModelPlugin<RealVector> {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "xnes";
    }

    /**
     * Returns a short human-readable component description.
     *
     * @return human-readable model description
     */
    @Override
    public String description() {
        return "xNES strategy model";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params model parameter map from YAML configuration
     * @return created component
     */
    @Override
    public XNesModel create(Map<String, Object> params) {
        return new XNesModel(
                Params.dbl(params, "etaMean", 0.5),
                Params.dbl(params, "etaCovariance", 0.15),
                Params.dbl(params, "jitter", 1.0e-9)
        );
    }
}
