package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Separable NES driver
 */
public final class SNesAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public SNesAlgorithm(double selectionRatio) {
        super("snes", selectionRatio);
    }
}
