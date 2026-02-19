package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Plugin for MIMIC scaffold driver.
 */
public final class MimicAlgorithmPlugin extends BaseRatioAlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "mimic";
    }

    @Override
    public String description() {
        return "MIMIC scaffold driver";
    }
}
