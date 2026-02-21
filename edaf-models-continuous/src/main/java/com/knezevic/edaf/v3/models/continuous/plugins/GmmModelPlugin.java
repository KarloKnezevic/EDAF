package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.GmmModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for diagonal-covariance Gaussian Mixture model.
 */
public final class GmmModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "gmm";
    }

    @Override
    public String description() {
        return "Gaussian mixture model with EM updates";
    }

    @Override
    public GmmModel create(Map<String, Object> params) {
        return new GmmModel(
                Params.integer(params, "components", 3),
                Params.integer(params, "emIterations", 20),
                Params.dbl(params, "minVariance", 1.0e-8)
        );
    }
}
