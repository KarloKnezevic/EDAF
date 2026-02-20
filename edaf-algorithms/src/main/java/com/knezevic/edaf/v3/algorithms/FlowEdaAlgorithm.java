package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Flow-based continuous EDA driver.
 */
public final class FlowEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public FlowEdaAlgorithm(double selectionRatio) {
        super("flow-eda", selectionRatio);
    }
}
