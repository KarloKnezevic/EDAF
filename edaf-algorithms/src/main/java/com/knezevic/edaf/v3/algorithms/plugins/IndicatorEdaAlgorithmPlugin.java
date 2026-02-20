package com.knezevic.edaf.v3.algorithms.plugins;


/**
 * Plugin alias for indicator-based EDA.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated indicator-based EDA update equations.</p>
 */
public final class IndicatorEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<Object> {

    @Override
    public String type() {
        return "indicator-eda";
    }

    @Override
    public String description() {
        return "indicator-based EDA scaffold driver";
    }
}
