package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Plugin for xNES scaffold driver.
 */
public final class XNesAlgorithmPlugin extends BaseRatioAlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "xnes";
    }

    @Override
    public String description() {
        return "xNES scaffold driver";
    }
}
