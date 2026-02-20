package com.knezevic.edaf.v3.algorithms.plugins;


/**
 * Plugin alias for noisy optimization EDA with resampling.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated noisy optimization EDA with resampling update equations.</p>
 */
public final class NoisyResamplingEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<Object> {

    @Override
    public String type() {
        return "noisy-resampling-eda";
    }

    @Override
    public String description() {
        return "noisy optimization EDA with resampling scaffold driver";
    }
}
