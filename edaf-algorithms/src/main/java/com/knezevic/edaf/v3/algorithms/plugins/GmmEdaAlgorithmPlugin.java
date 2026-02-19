package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Plugin for GMM-EDA scaffold driver.
 */
public final class GmmEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "gmm-eda";
    }

    @Override
    public String description() {
        return "GMM-EDA scaffold driver";
    }
}
