/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.plugins.discrete;

import com.knezevic.edaf.v3.algorithms.UmdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Literature alias for binary UMDA (UMDAd).
 */
public final class UmdadAlgorithmPlugin implements AlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "umdad";
    }

    @Override
    public String description() {
        return "UMDAd (binary UMDA) literature alias";
    }

    @Override
    public Algorithm<BitString> create(AlgorithmDependencies<BitString> dependencies, Map<String, Object> params) {
        return new UmdaAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
