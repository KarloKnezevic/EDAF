package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.continuous.NormalizingFlowModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for lightweight normalizing-flow model.
 */
public final class NormalizingFlowModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "normalizing-flow";
    }

    @Override
    public String description() {
        return "Autoregressive tanh flow with covariance transport";
    }

    @Override
    public NormalizingFlowModel create(Map<String, Object> params) {
        return new NormalizingFlowModel(
                Params.dbl(params, "jitter", 1e-9),
                Params.dbl(params, "learningRate", 0.7),
                Params.dbl(params, "maxSkew", 0.8)
        );
    }
}
