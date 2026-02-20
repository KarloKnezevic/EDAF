package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Plugin alias for Chow-Liu tree EDA.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated Chow-Liu tree EDA update equations.</p>
 */
public final class ChowLiuEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "chow-liu-eda";
    }

    @Override
    public String description() {
        return "Chow-Liu tree EDA scaffold driver";
    }
}
