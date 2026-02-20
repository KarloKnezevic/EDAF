package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Full-covariance continuous EDA driver.
 */
public final class FullCovarianceEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public FullCovarianceEdaAlgorithm(double selectionRatio) {
        super("full-covariance-eda", selectionRatio);
    }
}
