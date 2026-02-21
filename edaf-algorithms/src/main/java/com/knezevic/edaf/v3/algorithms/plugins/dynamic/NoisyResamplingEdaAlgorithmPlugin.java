package com.knezevic.edaf.v3.algorithms.plugins.dynamic;

import com.knezevic.edaf.v3.algorithms.dynamic.NoisyResamplingEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;

import java.util.Map;

/**
 * Plugin for noisy optimization EDA with resampling.
 */
public final class NoisyResamplingEdaAlgorithmPlugin implements AlgorithmPlugin<Object> {

    @Override
    public String type() {
        return "noisy-resampling-eda";
    }

    @Override
    public String description() {
        return "Noisy-resampling EDA driver";
    }

    @Override
    public Algorithm<Object> create(AlgorithmDependencies<Object> dependencies, Map<String, Object> params) {
        return new NoisyResamplingEdaAlgorithm<>(
                Params.dbl(params, "selectionRatio", 0.5),
                Params.dbl(params, "minSelectionRatio", 0.2),
                Params.dbl(params, "maxSelectionRatio", 0.9),
                Params.integer(params, "resamples", 3),
                Params.dbl(params, "noiseThreshold", 1.0e-4),
                Params.dbl(params, "adjustmentStep", 0.02)
        );
    }
}
