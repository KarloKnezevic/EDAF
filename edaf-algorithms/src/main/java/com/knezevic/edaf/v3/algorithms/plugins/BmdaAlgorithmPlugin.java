package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Plugin for BMDA scaffold driver.
 */
public final class BmdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "bmda";
    }

    @Override
    public String description() {
        return "BMDA scaffold driver";
    }
}
