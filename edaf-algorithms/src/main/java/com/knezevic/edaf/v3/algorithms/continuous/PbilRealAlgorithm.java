package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Real-coded PBIL driver
 */
public final class PbilRealAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public PbilRealAlgorithm(double selectionRatio) {
        super("pbil-real", selectionRatio);
    }
}
