package com.knezevic.edaf.v3.algorithms.plugins.discrete;

import com.knezevic.edaf.v3.algorithms.discrete.PbilAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for Population-Based Incremental Learning driver
 */
public final class PbilAlgorithmPlugin implements AlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "pbil";
    }

    @Override
    public String description() {
        return "Population-Based Incremental Learning driver";
    }

    @Override
    public Algorithm<BitString> create(AlgorithmDependencies<BitString> dependencies, Map<String, Object> params) {
        return new PbilAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
