package com.knezevic.edaf.v3.algorithms.plugins.discrete;

import com.knezevic.edaf.v3.algorithms.discrete.BoaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for Bayesian Optimization Algorithm driver
 */
public final class BoaAlgorithmPlugin implements AlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "boa";
    }

    @Override
    public String description() {
        return "Bayesian Optimization Algorithm driver";
    }

    @Override
    public Algorithm<BitString> create(AlgorithmDependencies<BitString> dependencies, Map<String, Object> params) {
        return new BoaAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
