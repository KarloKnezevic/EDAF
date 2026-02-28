/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.discrete.BmdaModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for BMDA bivariate dependency model.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BmdaModelPlugin implements ModelPlugin<BitString> {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "bmda";
    }

    /**
     * Returns a short human-readable component description.
     *
     * @return human-readable model description
     */
    @Override
    public String description() {
        return "BMDA dependency-tree model with conditional Bernoulli sampling";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params model parameter map from YAML configuration
     * @return created component
     */
    @Override
    public BmdaModel create(Map<String, Object> params) {
        return new BmdaModel(Params.dbl(params, "smoothing", 0.5));
    }
}
