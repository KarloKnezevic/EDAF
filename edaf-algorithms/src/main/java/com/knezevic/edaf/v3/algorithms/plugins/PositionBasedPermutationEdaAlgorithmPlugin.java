package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Plugin alias for position-based permutation EDA.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated position-based permutation EDA update equations.</p>
 */
public final class PositionBasedPermutationEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<PermutationVector> {

    @Override
    public String type() {
        return "position-based-permutation-eda";
    }

    @Override
    public String description() {
        return "position-based permutation EDA scaffold driver";
    }
}
