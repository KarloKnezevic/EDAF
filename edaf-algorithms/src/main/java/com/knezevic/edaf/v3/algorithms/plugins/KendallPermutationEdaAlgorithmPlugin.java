package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Plugin alias for Kendall-distance permutation EDA.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated Kendall-distance permutation EDA update equations.</p>
 */
public final class KendallPermutationEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<PermutationVector> {

    @Override
    public String type() {
        return "kendall-permutation-eda";
    }

    @Override
    public String description() {
        return "Kendall-distance permutation EDA scaffold driver";
    }
}
