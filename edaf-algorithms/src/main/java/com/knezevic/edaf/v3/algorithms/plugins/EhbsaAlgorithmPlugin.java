package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Plugin alias for Edge Histogram Based Sampling Algorithm.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated Edge Histogram Based Sampling Algorithm update equations.</p>
 */
public final class EhbsaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<PermutationVector> {

    @Override
    public String type() {
        return "ehbsa";
    }

    @Override
    public String description() {
        return "Edge Histogram Based Sampling Algorithm scaffold driver";
    }
}
