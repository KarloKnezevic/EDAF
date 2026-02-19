package com.knezevic.edaf.v3.algorithms;

/**
 * Multi-objective EDA skeleton using scalarized fallback.
 *
 * TODO(priority=high): Replace scalar fallback with Pareto archive + dominance ranking + MO sampling.
 */
public final class MoEdaSkeletonAlgorithm<G> extends RatioBasedEdaAlgorithm<G> {

    public MoEdaSkeletonAlgorithm(double selectionRatio) {
        super("mo-eda-skeleton", selectionRatio);
    }
}
