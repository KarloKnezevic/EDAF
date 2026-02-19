package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Plugin for sNES scaffold driver.
 */
public final class SNesAlgorithmPlugin extends BaseRatioAlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "snes";
    }

    @Override
    public String description() {
        return "sNES scaffold driver";
    }
}
