/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.discrete.PbilFrequencyModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for PBIL frequency model.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class PbilFrequencyModelPlugin implements ModelPlugin<BitString> {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "pbil-frequency";
    }

    /**
     * Returns a short human-readable component description.
     *
     * @return human-readable model description
     */
    @Override
    public String description() {
        return "PBIL moving-average frequency model";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params model parameter map from YAML configuration
     * @return created component
     */
    @Override
    public PbilFrequencyModel create(Map<String, Object> params) {
        return new PbilFrequencyModel(Params.dbl(params, "learningRate", 0.1));
    }
}
