package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Plugin for EBNA scaffold driver.
 */
public final class EbnaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "ebna";
    }

    @Override
    public String description() {
        return "EBNA scaffold driver";
    }
}
