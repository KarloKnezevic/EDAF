package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * UMDA algorithm driver.
 */
public final class UmdaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    public UmdaAlgorithm(double selectionRatio) {
        super("umda", selectionRatio);
    }
}
