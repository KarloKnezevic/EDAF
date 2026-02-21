package com.knezevic.edaf.v3.algorithms.mo;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

/**
 * Baseline multi-objective EDA driver using scalarized selection.
 */
public final class MoEdaSkeletonAlgorithm<G> extends RatioBasedEdaAlgorithm<G> {

    public MoEdaSkeletonAlgorithm(double selectionRatio) {
        super("mo-eda-skeleton", selectionRatio);
    }
}
