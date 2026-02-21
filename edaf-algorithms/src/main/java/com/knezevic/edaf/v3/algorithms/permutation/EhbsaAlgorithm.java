package com.knezevic.edaf.v3.algorithms.permutation;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Edge Histogram Based Sampling Algorithm (EHBSA) driver.
 */
public final class EhbsaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    public EhbsaAlgorithm(double selectionRatio) {
        super("ehbsa", selectionRatio);
    }
}
