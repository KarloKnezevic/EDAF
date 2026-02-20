package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Plugin alias for block covariance EDA.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated block covariance EDA update equations.</p>
 */
public final class BlockCovarianceEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "block-covariance-eda";
    }

    @Override
    public String description() {
        return "block covariance EDA scaffold driver";
    }
}
