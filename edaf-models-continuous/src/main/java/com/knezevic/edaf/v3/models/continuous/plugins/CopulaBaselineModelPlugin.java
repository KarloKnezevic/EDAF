package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.CopulaBaselineModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for Gaussian-copula baseline model.
 */
public final class CopulaBaselineModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "copula-baseline";
    }

    @Override
    public String description() {
        return "Gaussian copula with empirical marginals";
    }

    @Override
    public CopulaBaselineModel create(Map<String, Object> params) {
        return new CopulaBaselineModel(Params.dbl(params, "jitter", 1.0e-9));
    }
}
