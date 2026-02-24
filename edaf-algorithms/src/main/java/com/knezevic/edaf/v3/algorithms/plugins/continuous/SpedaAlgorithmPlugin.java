/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.plugins.continuous;

import com.knezevic.edaf.v3.algorithms.continuous.GmmEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Literature alias for SPEDA.
 */
public final class SpedaAlgorithmPlugin implements AlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "speda";
    }

    @Override
    public String description() {
        return "SPEDA literature alias (mapped to Gaussian-mixture EDA driver)";
    }

    @Override
    public Algorithm<RealVector> create(AlgorithmDependencies<RealVector> dependencies, Map<String, Object> params) {
        return new GmmEdaAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
