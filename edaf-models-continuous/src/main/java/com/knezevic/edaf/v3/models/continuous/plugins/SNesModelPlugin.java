/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.SNesModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for separable NES model.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class SNesModelPlugin implements ModelPlugin<RealVector> {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "snes";
    }

    /**
     * Returns a short human-readable component description.
     *
     * @return human-readable model description
     */
    @Override
    public String description() {
        return "Separable NES strategy model";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params model parameter map from YAML configuration
     * @return created component
     */
    @Override
    public SNesModel create(Map<String, Object> params) {
        return new SNesModel(
                Params.dbl(params, "etaMean", 0.6),
                Params.dbl(params, "etaSigma", 0.2),
                Params.dbl(params, "minSigma", 1.0e-8),
                Params.dbl(params, "maxSigma", 10.0)
        );
    }
}
