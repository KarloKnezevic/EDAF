/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.plugins.continuous;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.impl.MixedRealDiscreteVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.MixedRealDiscreteVector;

import java.util.List;
import java.util.Map;

/**
 * Plugin factory for mixed real/discrete vectors.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MixedRealDiscreteVectorRepresentationPlugin implements RepresentationPlugin<MixedRealDiscreteVector> {

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "mixed-real-discrete-vector";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Mixed real and discrete representation";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return the create
     */
    @Override
    public MixedRealDiscreteVectorRepresentation create(Map<String, Object> params) {
        int realDimensions = Params.integer(params, "realDimensions", 8);
        List<Object> cards = Params.list(params, "cardinalities");
        int[] cardinalities;
        if (cards.isEmpty()) {
            cardinalities = new int[]{4, 4};
        } else {
            cardinalities = cards.stream().mapToInt(value -> Integer.parseInt(String.valueOf(value))).toArray();
        }
        double lower = Params.dbl(params, "lower", -5.0);
        double upper = Params.dbl(params, "upper", 5.0);
        return new MixedRealDiscreteVectorRepresentation(realDimensions, cardinalities, lower, upper);
    }
}
