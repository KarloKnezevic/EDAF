package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Plugin for compact GA scaffold driver.
 */
public final class CgaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "cga";
    }

    @Override
    public String description() {
        return "Compact GA driver using configured model and policies";
    }
}
