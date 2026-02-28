/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.plugins.structured;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.impl.VariableLengthVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.util.Map;

/**
 * Plugin factory for variable-length vectors.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class VariableLengthVectorRepresentationPlugin implements RepresentationPlugin<VariableLengthVector<Integer>> {

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "variable-length-vector";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Variable-length integer token representation";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return the create
     */
    @Override
    public VariableLengthVectorRepresentation create(Map<String, Object> params) {
        int min = Params.integer(params, "minLength", 2);
        int max = Params.integer(params, "maxLength", 16);
        int maxToken = Params.integer(params, "maxToken", 64);
        return new VariableLengthVectorRepresentation(min, max, maxToken);
    }
}
