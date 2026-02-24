/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.plugins.continuous;

import com.knezevic.edaf.v3.algorithms.continuous.UmdaContinuousAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Literature alias for continuous UMDA (UMDAc).
 */
public final class UmdacAlgorithmPlugin implements AlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "umdac";
    }

    @Override
    public String description() {
        return "UMDAc (continuous UMDA) literature alias";
    }

    @Override
    public Algorithm<RealVector> create(AlgorithmDependencies<RealVector> dependencies, Map<String, Object> params) {
        return new UmdaContinuousAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
