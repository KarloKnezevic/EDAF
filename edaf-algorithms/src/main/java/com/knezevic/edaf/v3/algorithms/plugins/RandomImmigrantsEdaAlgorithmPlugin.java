package com.knezevic.edaf.v3.algorithms.plugins;


/**
 * Plugin alias for random-immigrants dynamic EDA.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated random-immigrants dynamic EDA update equations.</p>
 */
public final class RandomImmigrantsEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<Object> {

    @Override
    public String type() {
        return "random-immigrants-eda";
    }

    @Override
    public String description() {
        return "random-immigrants dynamic EDA scaffold driver";
    }
}
