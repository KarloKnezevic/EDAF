package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * EBNA driver
 */
public final class EbnaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    public EbnaAlgorithm(double selectionRatio) {
        super("ebna", selectionRatio);
    }
}
