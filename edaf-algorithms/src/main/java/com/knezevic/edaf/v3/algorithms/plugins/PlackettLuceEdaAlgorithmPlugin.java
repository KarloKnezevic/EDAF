package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Plugin for Plackett-Luce EDA scaffold driver.
 */
public final class PlackettLuceEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<PermutationVector> {

    @Override
    public String type() {
        return "plackett-luce-eda";
    }

    @Override
    public String description() {
        return "Plackett-Luce EDA scaffold driver";
    }
}
