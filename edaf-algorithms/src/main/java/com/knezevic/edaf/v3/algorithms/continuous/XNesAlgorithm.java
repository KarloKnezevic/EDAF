package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * xNES driver
 */
public final class XNesAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public XNesAlgorithm(double selectionRatio) {
        super("xnes", selectionRatio);
    }
}
