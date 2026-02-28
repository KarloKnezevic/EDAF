/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.plugins.structured;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.repr.impl.GrammarBitStringRepresentation;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for grammar-driven symbolic GP representation encoded as bitstring decisions.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class GrammarBitStringRepresentationPlugin implements RepresentationPlugin<BitString> {

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "grammar-bitstring";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Grammar-based symbolic representation encoded as fixed-length bitstring decisions";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return the create
     */
    @Override
    public GrammarBitStringRepresentation create(Map<String, Object> params) {
        return new GrammarBitStringRepresentation(params);
    }
}
