/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.discrete.BernoulliUmdaModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for UMDA Bernoulli model.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class UmdaBernoulliModelPlugin implements ModelPlugin<BitString> {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "umda-bernoulli";
    }

    /**
     * Returns a short human-readable component description.
     *
     * @return human-readable model description
     */
    @Override
    public String description() {
        return "Univariate Bernoulli model for UMDA";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params model parameter map from YAML configuration
     * @return created component
     */
    @Override
    public BernoulliUmdaModel create(Map<String, Object> params) {
        return new BernoulliUmdaModel(Params.dbl(params, "smoothing", 0.01));
    }
}
