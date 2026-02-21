package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Continuous MIMIC driver
 */
public final class MimicContinuousAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public MimicContinuousAlgorithm(double selectionRatio) {
        super("mimic-continuous", selectionRatio);
    }
}
