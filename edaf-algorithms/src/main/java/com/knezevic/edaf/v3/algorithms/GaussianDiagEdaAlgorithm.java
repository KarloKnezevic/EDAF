package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Gaussian diagonal EDA algorithm driver.
 */
public final class GaussianDiagEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public GaussianDiagEdaAlgorithm(double selectionRatio) {
        super("gaussian-eda", selectionRatio);
    }
}
