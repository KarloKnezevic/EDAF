package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * MIMIC driver
 */
public final class MimicAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    public MimicAlgorithm(double selectionRatio) {
        super("mimic", selectionRatio);
    }
}
