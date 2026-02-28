/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.impl.IntVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.IntVector;

import java.util.Map;

/**
 * Plugin factory for bounded integer vectors.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class IntVectorRepresentationPlugin implements RepresentationPlugin<IntVector> {

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "int-vector";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Bounded integer vector representation";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return the create
     */
    @Override
    public IntVectorRepresentation create(Map<String, Object> params) {
        int length = Params.integer(params, "length", 32);
        int min = Params.integer(params, "min", 0);
        int max = Params.integer(params, "max", 10);
        return new IntVectorRepresentation(length, min, max);
    }
}
