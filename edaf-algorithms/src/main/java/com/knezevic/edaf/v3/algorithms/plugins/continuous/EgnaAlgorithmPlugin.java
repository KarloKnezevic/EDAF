/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.plugins.continuous;

import com.knezevic.edaf.v3.algorithms.FullCovarianceEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Literature alias for EGNA-style continuous dependency modeling.
 */
public final class EgnaAlgorithmPlugin implements AlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "egna";
    }

    @Override
    public String description() {
        return "EGNA literature alias (mapped to full-covariance Gaussian driver)";
    }

    @Override
    public Algorithm<RealVector> create(AlgorithmDependencies<RealVector> dependencies, Map<String, Object> params) {
        return new FullCovarianceEdaAlgorithm(Params.dbl(params, "selectionRatio", 0.4));
    }
}
