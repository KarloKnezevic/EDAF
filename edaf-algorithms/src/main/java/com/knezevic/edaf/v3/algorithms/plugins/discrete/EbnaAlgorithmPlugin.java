package com.knezevic.edaf.v3.algorithms.plugins.discrete;

import com.knezevic.edaf.v3.algorithms.discrete.EbnaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for EBNA driver
 */
public final class EbnaAlgorithmPlugin implements AlgorithmPlugin<BitString> {

    @Override
    public String type() {
        return "ebna";
    }

    @Override
    public String description() {
        return "EBNA driver";
    }

    @Override
    public Algorithm<BitString> create(AlgorithmDependencies<BitString> dependencies, Map<String, Object> params) {
        return new EbnaAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
