package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Hierarchical BOA algorithm driver.
 */
public final class HBoaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    public HBoaAlgorithm(double selectionRatio) {
        super("hboa", selectionRatio);
    }
}
