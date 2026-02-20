package com.knezevic.edaf.v3.algorithms.plugins;


/**
 * Plugin alias for memory-based dynamic EDA.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated memory-based dynamic EDA update equations.</p>
 */
public final class MemoryEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<Object> {

    @Override
    public String type() {
        return "memory-eda";
    }

    @Override
    public String description() {
        return "memory-based dynamic EDA scaffold driver";
    }
}
