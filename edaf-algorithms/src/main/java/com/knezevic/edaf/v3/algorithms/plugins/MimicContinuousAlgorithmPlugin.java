package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Plugin alias for continuous MIMIC.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated continuous MIMIC update equations.</p>
 */
public final class MimicContinuousAlgorithmPlugin extends BaseRatioAlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "mimic-continuous";
    }

    @Override
    public String description() {
        return "continuous MIMIC scaffold driver";
    }
}
