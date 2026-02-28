/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.plugins.permutation;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.impl.PermutationVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
 * Plugin factory for permutation vectors.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class PermutationVectorRepresentationPlugin implements RepresentationPlugin<PermutationVector> {

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "permutation-vector";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Permutation representation";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return the create
     */
    @Override
    public PermutationVectorRepresentation create(Map<String, Object> params) {
        int size = Params.integer(params, "size", Params.integer(params, "length", 20));
        return new PermutationVectorRepresentation(size);
    }
}
