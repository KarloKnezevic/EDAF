package com.knezevic.edaf.v3.algorithms.plugins.discrete;

import com.knezevic.edaf.v3.algorithms.discrete.FactorizedDiscreteEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for Factorized-discrete EDA driver
 */
public final class FactorizedDiscreteEdaAlgorithmPlugin implements AlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "factorized-discrete-eda";
    }

    @Override
    public String description() {
        return "Factorized-discrete EDA driver";
    }

    @Override
    public Algorithm<BitString> create(AlgorithmDependencies<BitString> dependencies, Map<String, Object> params) {
        return new FactorizedDiscreteEdaAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
