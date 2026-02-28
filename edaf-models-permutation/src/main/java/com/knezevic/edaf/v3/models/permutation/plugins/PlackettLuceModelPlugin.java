/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.models.permutation.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.models.permutation.PlackettLuceModel;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
 * Plugin factory for Plackett-Luce permutation model.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class PlackettLuceModelPlugin implements ModelPlugin<PermutationVector> {

    /**
     * Returns component type identifier.
     *
     * @return component type
     */
    @Override
    public String type() {
        return "plackett-luce";
    }

    /**
     * Returns a short human-readable component description.
     *
     * @return human-readable model description
     */
    @Override
    public String description() {
        return "Plackett-Luce permutation model";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params model parameter map from YAML configuration
     * @return created component
     */
    @Override
    public PlackettLuceModel create(Map<String, Object> params) {
        return new PlackettLuceModel();
    }
}
