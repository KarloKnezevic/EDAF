package com.knezevic.edaf.v3.algorithms.plugins.continuous;

import com.knezevic.edaf.v3.algorithms.continuous.BlockCovarianceEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin for Block covariance EDA driver
 */
public final class BlockCovarianceEdaAlgorithmPlugin implements AlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "block-covariance-eda";
    }

    @Override
    public String description() {
        return "Block covariance EDA driver";
    }

    @Override
    public Algorithm<RealVector> create(AlgorithmDependencies<RealVector> dependencies, Map<String, Object> params) {
        return new BlockCovarianceEdaAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
