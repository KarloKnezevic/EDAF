package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Plugin alias for factorized distribution EDA.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated factorized distribution EDA update equations.</p>
 */
public final class FactorizedDiscreteEdaAlgorithmPlugin extends BaseRatioAlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "factorized-discrete-eda";
    }

    @Override
    public String description() {
        return "factorized distribution EDA scaffold driver";
    }
}
