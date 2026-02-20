package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Plugin alias for continuous UMDA.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated continuous UMDA update equations.</p>
 */
public final class UmdaContinuousAlgorithmPlugin extends BaseRatioAlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "umda-continuous";
    }

    @Override
    public String description() {
        return "continuous UMDA scaffold driver";
    }
}
