package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Compact GA driver using configured model and policies
 */
public final class CgaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    public CgaAlgorithm(double selectionRatio) {
        super("cga", selectionRatio);
    }
}
