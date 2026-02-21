package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Chow-Liu tree EDA driver
 */
public final class ChowLiuEdaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    public ChowLiuEdaAlgorithm(double selectionRatio) {
        super("chow-liu-eda", selectionRatio);
    }
}
