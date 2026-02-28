/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.discrete.token.TokenCategoricalModel;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.util.Map;

/**
 * Plugin factory for variable-length token categorical model.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class TokenCategoricalModelPlugin implements ModelPlugin<VariableLengthVector<Integer>> {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "token-categorical";
    }

    /**
     * Returns a short human-readable component description.
     *
     * @return human-readable model description
     */
    @Override
    public String description() {
        return "Categorical model for variable-length integer token sequences";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params model parameter map from YAML configuration
     * @return created component
     */
    @Override
    public TokenCategoricalModel create(Map<String, Object> params) {
        int maxToken = Params.integer(params, "maxToken", 64);
        double smoothing = Params.dbl(params, "smoothing", 0.01);
        return new TokenCategoricalModel(maxToken, smoothing);
    }
}
