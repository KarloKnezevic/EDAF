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
 */
public final class GrammarBitStringRepresentationPlugin implements RepresentationPlugin<BitString> {

    @Override
    public String type() {
        return "grammar-bitstring";
    }

    @Override
    public String description() {
        return "Grammar-based symbolic representation encoded as fixed-length bitstring decisions";
    }

    @Override
    public GrammarBitStringRepresentation create(Map<String, Object> params) {
        return new GrammarBitStringRepresentation(params);
    }
}
