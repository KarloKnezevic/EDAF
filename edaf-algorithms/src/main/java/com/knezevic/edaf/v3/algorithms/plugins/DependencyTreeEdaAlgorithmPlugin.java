package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Plugin alias for dependency-tree EDA.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated dependency-tree EDA update equations.</p>
 */
public final class DependencyTreeEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "dependency-tree-eda";
    }

    @Override
    public String description() {
        return "dependency-tree EDA scaffold driver";
    }
}
