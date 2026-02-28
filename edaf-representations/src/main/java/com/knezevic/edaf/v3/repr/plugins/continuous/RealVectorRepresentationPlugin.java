/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.plugins.continuous;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.impl.RealVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for real-valued vectors.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class RealVectorRepresentationPlugin implements RepresentationPlugin<RealVector> {

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "real-vector";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Real-valued vector representation";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return the create
     */
    @Override
    public RealVectorRepresentation create(Map<String, Object> params) {
        int length = Params.integer(params, "length", 16);
        double lower = Params.dbl(params, "lower", -5.0);
        double upper = Params.dbl(params, "upper", 5.0);
        return new RealVectorRepresentation(length, lower, upper);
    }
}
