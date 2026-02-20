package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Plugin alias for Cross-Entropy Method.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated Cross-Entropy Method update equations.</p>
 */
public final class CemAlgorithmPlugin extends BaseRatioAlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "cem";
    }

    @Override
    public String description() {
        return "Cross-Entropy Method scaffold driver";
    }
}
