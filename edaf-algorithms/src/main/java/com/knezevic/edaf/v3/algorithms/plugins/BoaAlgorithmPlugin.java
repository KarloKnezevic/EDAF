package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Plugin for BOA/EBNA scaffold driver.
 */
public final class BoaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "boa";
    }

    @Override
    public String description() {
        return "BOA scaffold driver";
    }
}
