package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Bayesian Optimization Algorithm driver
 */
public final class BoaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    public BoaAlgorithm(double selectionRatio) {
        super("boa", selectionRatio);
    }
}
