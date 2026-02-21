package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Cross-Entropy Method driver
 */
public final class CemAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public CemAlgorithm(double selectionRatio) {
        super("cem", selectionRatio);
    }
}
