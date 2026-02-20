package com.knezevic.edaf.v3.models.continuous.plugins;

import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.models.continuous.CmaEsStrategyModel;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for CMA-ES strategy model.
 */
public final class CmaEsStrategyModelPlugin implements ModelPlugin<RealVector> {

    @Override
    public String type() {
        return "cma-es";
    }

    @Override
    public String description() {
        return "CMA-ES strategy model with path and covariance adaptation";
    }

    @Override
    public CmaEsStrategyModel create(Map<String, Object> params) {
        double minSigma = Params.dbl(params, "minSigma", 1.0e-12);
        double maxSigma = Params.dbl(params, "maxSigma", 5.0);
        double jitter = Params.dbl(params, "jitter", 1.0e-12);
        double initialSigma = Params.dbl(params, "initialSigma", -1.0);
        boolean restartEnabled = Params.bool(params, "restartEnabled", true);
        int restartPatience = Params.integer(params, "restartPatience", 30);
        double restartSigmaMultiplier = Params.dbl(params, "restartSigmaMultiplier", 2.0);
        double restartConditionThreshold = Params.dbl(params, "restartConditionThreshold", 1.0e12);
        double restartImprovementEpsilon = Params.dbl(params, "restartImprovementEpsilon", 1.0e-12);

        return new CmaEsStrategyModel(
                minSigma,
                maxSigma,
                jitter,
                initialSigma,
                restartEnabled,
                restartPatience,
                restartSigmaMultiplier,
                restartConditionThreshold,
                restartImprovementEpsilon
        );
    }
}
