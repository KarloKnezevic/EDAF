package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Plugin alias for Information-Geometric Optimization.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated Information-Geometric Optimization update equations.</p>
 */
public final class IgoAlgorithmPlugin extends BaseRatioAlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "igo";
    }

    @Override
    public String description() {
        return "Information-Geometric Optimization scaffold driver";
    }
}
