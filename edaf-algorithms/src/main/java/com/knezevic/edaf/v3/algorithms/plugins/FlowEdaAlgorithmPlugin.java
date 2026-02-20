package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.algorithms.FlowEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin for flow-based continuous EDA.
 */
public final class FlowEdaAlgorithmPlugin implements AlgorithmPlugin<RealVector> {

    @Override
    public String type() {
        return "flow-eda";
    }

    @Override
    public String description() {
        return "Normalizing-flow EDA with nonlinear latent transport";
    }

    @Override
    public Algorithm<RealVector> create(AlgorithmDependencies<RealVector> dependencies, Map<String, Object> params) {
        return new FlowEdaAlgorithm(Params.dbl(params, "selectionRatio", 0.4));
    }
}
