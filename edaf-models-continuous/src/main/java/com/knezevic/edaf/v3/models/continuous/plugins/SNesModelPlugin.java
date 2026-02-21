package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.SNesModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for separable NES model.
 */
public final class SNesModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "snes";
    }

    @Override
    public String description() {
        return "Separable NES strategy model";
    }

    @Override
    public SNesModel create(Map<String, Object> params) {
        return new SNesModel(
                Params.dbl(params, "etaMean", 0.6),
                Params.dbl(params, "etaSigma", 0.2),
                Params.dbl(params, "minSigma", 1.0e-8),
                Params.dbl(params, "maxSigma", 10.0)
        );
    }
}
