package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.algorithms.UmdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for UMDA algorithm.
 */
public final class UmdaAlgorithmPlugin implements AlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "umda";
    }

    @Override
    public String description() {
        return "Univariate Marginal Distribution Algorithm";
    }

    @Override
    public Algorithm<BitString> create(AlgorithmDependencies<BitString> dependencies, Map<String, Object> params) {
        return new UmdaAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
