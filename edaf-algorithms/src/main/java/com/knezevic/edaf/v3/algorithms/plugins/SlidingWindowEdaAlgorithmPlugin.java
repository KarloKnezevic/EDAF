package com.knezevic.edaf.v3.algorithms.plugins;


/**
 * Plugin alias for sliding-window dynamic EDA.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated sliding-window dynamic EDA update equations.</p>
 */
public final class SlidingWindowEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<Object> {

    @Override
    public String type() {
        return "sliding-window-eda";
    }

    @Override
    public String description() {
        return "sliding-window dynamic EDA scaffold driver";
    }
}
