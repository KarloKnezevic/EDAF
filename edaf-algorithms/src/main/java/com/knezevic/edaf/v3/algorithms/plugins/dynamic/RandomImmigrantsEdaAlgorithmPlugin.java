package com.knezevic.edaf.v3.algorithms.plugins.dynamic;

import com.knezevic.edaf.v3.algorithms.dynamic.RandomImmigrantsEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;

import java.util.Map;

/**
 * Plugin for random-immigrants dynamic EDA.
 */
public final class RandomImmigrantsEdaAlgorithmPlugin implements AlgorithmPlugin<Object> {

    @Override
    public String type() {
        return "random-immigrants-eda";
    }

    @Override
    public String description() {
        return "Random-immigrants dynamic EDA driver";
    }

    @Override
    public Algorithm<Object> create(AlgorithmDependencies<Object> dependencies, Map<String, Object> params) {
        return new RandomImmigrantsEdaAlgorithm<>(
                Params.dbl(params, "selectionRatio", 0.5),
                Params.dbl(params, "minSelectionRatio", 0.2),
                Params.dbl(params, "maxSelectionRatio", 0.9),
                Params.dbl(params, "immigrantRatio", 0.1),
                Params.integer(params, "minImmigrants", 1)
        );
    }
}
