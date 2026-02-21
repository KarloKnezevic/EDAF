package com.knezevic.edaf.v3.algorithms.permutation;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Position-based permutation EDA driver.
 */
public final class PositionBasedPermutationEdaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    public PositionBasedPermutationEdaAlgorithm(double selectionRatio) {
        super("position-based-permutation-eda", selectionRatio);
    }
}
