package com.knezevic.edaf.v3.algorithms.plugins;


/**
 * Plugin alias for Pareto-based EDA.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated Pareto-based EDA update equations.</p>
 */
public final class ParetoEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<Object> {

    @Override
    public String type() {
        return "pareto-eda";
    }

    @Override
    public String description() {
        return "Pareto-based EDA scaffold driver";
    }
}
