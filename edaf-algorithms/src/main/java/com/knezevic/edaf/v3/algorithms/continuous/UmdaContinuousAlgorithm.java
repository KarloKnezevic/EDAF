package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Continuous UMDA driver
 */
public final class UmdaContinuousAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public UmdaContinuousAlgorithm(double selectionRatio) {
        super("umda-continuous", selectionRatio);
    }
}
