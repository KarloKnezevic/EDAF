package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * PBIL driver using configured model and policies
 */
public final class PbilAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    public PbilAlgorithm(double selectionRatio) {
        super("pbil", selectionRatio);
    }
}
