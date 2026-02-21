package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Information-Geometric Optimization driver
 */
public final class IgoAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public IgoAlgorithm(double selectionRatio) {
        super("igo", selectionRatio);
    }
}
