package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.XNesModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for xNES full-covariance model.
 */
public final class XNesModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "xnes";
    }

    @Override
    public String description() {
        return "xNES strategy model";
    }

    @Override
    public XNesModel create(Map<String, Object> params) {
        return new XNesModel(
                Params.dbl(params, "etaMean", 0.5),
                Params.dbl(params, "etaCovariance", 0.15),
                Params.dbl(params, "jitter", 1.0e-9)
        );
    }
}
