package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;

import java.util.Map;

/**
 * Shared plugin base for ratio-based algorithm scaffolds.
 */
abstract class BaseRatioAlgorithmPlugin<G> implements AlgorithmPlugin<G> {

    @Override
    public Algorithm<G> create(AlgorithmDependencies<G> dependencies, Map<String, Object> params) {
        double ratio = Params.dbl(params, "selectionRatio", 0.5);
        return new RatioBasedEdaAlgorithm<>(type(), ratio);
    }
}
