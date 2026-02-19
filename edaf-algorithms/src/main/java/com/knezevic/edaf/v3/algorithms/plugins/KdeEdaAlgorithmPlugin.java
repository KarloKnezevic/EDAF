package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Plugin for KDE-EDA scaffold driver.
 */
public final class KdeEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "kde-eda";
    }

    @Override
    public String description() {
        return "KDE-EDA scaffold driver";
    }
}
