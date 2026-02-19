package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.DiagonalGaussianModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for diagonal Gaussian model.
 */
public final class DiagonalGaussianModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "gaussian-diag";
    }

    @Override
    public String description() {
        return "Diagonal Gaussian density estimator";
    }

    @Override
    public DiagonalGaussianModel create(Map<String, Object> params) {
        return new DiagonalGaussianModel(Params.dbl(params, "minSigma", 1e-8));
    }
}
