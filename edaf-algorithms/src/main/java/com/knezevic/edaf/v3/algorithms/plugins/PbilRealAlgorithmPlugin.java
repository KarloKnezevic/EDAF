package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Plugin alias for real-coded PBIL.
 *
 * <p>Current implementation delegates to the shared ratio-based EDA driver scaffold.
 * TODO(priority=high): replace with dedicated real-coded PBIL update equations.</p>
 */
public final class PbilRealAlgorithmPlugin extends BaseRatioAlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "pbil-real";
    }

    @Override
    public String description() {
        return "real-coded PBIL scaffold driver";
    }
}
