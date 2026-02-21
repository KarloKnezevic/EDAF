package com.knezevic.edaf.v3.algorithms.permutation;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Mallows EDA driver
 */
public final class MallowsEdaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    public MallowsEdaAlgorithm(double selectionRatio) {
        super("mallows-eda", selectionRatio);
    }
}
