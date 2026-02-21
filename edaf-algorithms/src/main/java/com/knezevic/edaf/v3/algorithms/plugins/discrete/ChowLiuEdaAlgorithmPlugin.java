package com.knezevic.edaf.v3.algorithms.plugins.discrete;

import com.knezevic.edaf.v3.algorithms.discrete.ChowLiuEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for Chow-Liu tree EDA driver
 */
public final class ChowLiuEdaAlgorithmPlugin implements AlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "chow-liu-eda";
    }

    @Override
    public String description() {
        return "Chow-Liu tree EDA driver";
    }

    @Override
    public Algorithm<BitString> create(AlgorithmDependencies<BitString> dependencies, Map<String, Object> params) {
        return new ChowLiuEdaAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
