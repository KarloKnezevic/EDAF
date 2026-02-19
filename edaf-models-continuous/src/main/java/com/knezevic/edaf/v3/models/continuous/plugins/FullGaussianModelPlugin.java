package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.FullGaussianModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for full-covariance Gaussian model.
 */
public final class FullGaussianModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "gaussian-full";
    }

    @Override
    public String description() {
        return "Full covariance Gaussian density estimator";
    }

    @Override
    public FullGaussianModel create(Map<String, Object> params) {
        return new FullGaussianModel(Params.dbl(params, "jitter", 1e-9));
    }
}
