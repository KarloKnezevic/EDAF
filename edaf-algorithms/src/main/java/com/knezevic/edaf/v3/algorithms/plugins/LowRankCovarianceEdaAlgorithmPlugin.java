package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Plugin alias for low-rank covariance EDA.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated low-rank covariance EDA update equations.</p>
 */
public final class LowRankCovarianceEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "lowrank-covariance-eda";
    }

    @Override
    public String description() {
        return "low-rank covariance EDA scaffold driver";
    }
}
