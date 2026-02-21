package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Factorized-discrete EDA driver
 */
public final class FactorizedDiscreteEdaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    public FactorizedDiscreteEdaAlgorithm(double selectionRatio) {
        super("factorized-discrete-eda", selectionRatio);
    }
}
