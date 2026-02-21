package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Block covariance EDA driver
 */
public final class BlockCovarianceEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public BlockCovarianceEdaAlgorithm(double selectionRatio) {
        super("block-covariance-eda", selectionRatio);
    }
}
