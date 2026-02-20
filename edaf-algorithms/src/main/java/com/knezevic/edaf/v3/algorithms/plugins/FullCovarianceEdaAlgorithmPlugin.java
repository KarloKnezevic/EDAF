package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.algorithms.FullCovarianceEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin for full-covariance EDA.
 */
public final class FullCovarianceEdaAlgorithmPlugin implements AlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "full-covariance-eda";
    }

    @Override
    public String description() {
        return "Full covariance EDA with adaptive covariance updates";
    }

    @Override
    public Algorithm<RealVector> create(AlgorithmDependencies<RealVector> dependencies, Map<String, Object> params) {
        return new FullCovarianceEdaAlgorithm(Params.dbl(params, "selectionRatio", 0.4));
    }
}
