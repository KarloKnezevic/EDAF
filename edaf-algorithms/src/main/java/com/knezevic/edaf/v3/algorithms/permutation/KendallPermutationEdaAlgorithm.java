package com.knezevic.edaf.v3.algorithms.permutation;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Kendall-distance permutation EDA driver.
 */
public final class KendallPermutationEdaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    public KendallPermutationEdaAlgorithm(double selectionRatio) {
        super("kendall-permutation-eda", selectionRatio);
    }
}
