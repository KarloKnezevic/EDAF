package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.KdeModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for KDE model.
 */
public final class KdeModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "kde";
    }

    @Override
    public String description() {
        return "Kernel density estimator with adaptive bandwidth";
    }

    @Override
    public KdeModel create(Map<String, Object> params) {
        return new KdeModel(
                Params.dbl(params, "bandwidth", 1.0),
                Params.dbl(params, "minBandwidth", 1.0e-8)
        );
    }
}
