package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Plugin for Copula-EDA scaffold driver.
 */
public final class CopulaEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "copula-eda";
    }

    @Override
    public String description() {
        return "Copula-EDA scaffold driver";
    }
}
