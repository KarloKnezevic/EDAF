package com.knezevic.edaf.v3.algorithms.permutation;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Plackett-Luce EDA driver
 */
public final class PlackettLuceEdaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    public PlackettLuceEdaAlgorithm(double selectionRatio) {
        super("plackett-luce-eda", selectionRatio);
    }
}
