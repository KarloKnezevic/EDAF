/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.impl.CategoricalVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.CategoricalVector;

import java.util.List;
import java.util.Map;

/**
 * Plugin factory for categorical vectors.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class CategoricalVectorRepresentationPlugin implements RepresentationPlugin<CategoricalVector> {

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "categorical-vector";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Categorical vector representation";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return the create
     */
    @Override
    public CategoricalVectorRepresentation create(Map<String, Object> params) {
        int length = Params.integer(params, "length", 16);
        List<Object> symbolsRaw = Params.list(params, "symbols");
        List<String> symbols = symbolsRaw.isEmpty()
                ? List.of("A", "B", "C")
                : symbolsRaw.stream().map(String::valueOf).toList();
        return new CategoricalVectorRepresentation(length, symbols);
    }
}
