package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Low-rank covariance EDA driver
 */
public final class LowRankCovarianceEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public LowRankCovarianceEdaAlgorithm(double selectionRatio) {
        super("lowrank-covariance-eda", selectionRatio);
    }
}
