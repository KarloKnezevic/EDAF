package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Plugin for Mallows EDA scaffold driver.
 */
public final class MallowsEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<PermutationVector> {

    @Override
    public String type() {
        return "mallows-eda";
    }

    @Override
    public String description() {
        return "Mallows EDA scaffold driver";
    }
}
