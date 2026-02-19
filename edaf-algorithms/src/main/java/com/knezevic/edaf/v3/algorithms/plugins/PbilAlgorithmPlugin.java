package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Plugin for PBIL algorithm scaffold driver.
 */
public final class PbilAlgorithmPlugin extends BaseRatioAlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "pbil";
    }

    @Override
    public String description() {
        return "PBIL driver using configured model and policies";
    }
}
