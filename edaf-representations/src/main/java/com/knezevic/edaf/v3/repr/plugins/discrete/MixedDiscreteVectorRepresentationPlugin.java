/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.impl.MixedDiscreteVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.MixedDiscreteVector;

import java.util.List;
import java.util.Map;

/**
 * Plugin factory for mixed discrete vectors.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MixedDiscreteVectorRepresentationPlugin implements RepresentationPlugin<MixedDiscreteVector> {

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "mixed-discrete-vector";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Mixed discrete vector representation";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return the create
     */
    @Override
    public MixedDiscreteVectorRepresentation create(Map<String, Object> params) {
        List<Object> cards = Params.list(params, "cardinalities");
        int[] cardinalities;
        if (cards.isEmpty()) {
            int length = Params.integer(params, "length", 8);
            int defaultCardinality = Params.integer(params, "cardinality", 4);
            cardinalities = new int[length];
            for (int i = 0; i < length; i++) {
                cardinalities[i] = defaultCardinality;
            }
        } else {
            cardinalities = cards.stream().mapToInt(value -> Integer.parseInt(String.valueOf(value))).toArray();
        }
        return new MixedDiscreteVectorRepresentation(cardinalities);
    }
}
