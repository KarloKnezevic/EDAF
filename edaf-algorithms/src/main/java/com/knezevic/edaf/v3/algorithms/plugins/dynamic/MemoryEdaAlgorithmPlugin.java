package com.knezevic.edaf.v3.algorithms.plugins.dynamic;

import com.knezevic.edaf.v3.algorithms.dynamic.MemoryEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;

import java.util.Map;

/**
 * Plugin for memory-based dynamic EDA.
 */
public final class MemoryEdaAlgorithmPlugin implements AlgorithmPlugin<Object> {

    @Override
    public String type() {
        return "memory-eda";
    }

    @Override
    public String description() {
        return "Memory-based dynamic EDA driver";
    }

    @Override
    public Algorithm<Object> create(AlgorithmDependencies<Object> dependencies, Map<String, Object> params) {
        return new MemoryEdaAlgorithm<>(
                Params.dbl(params, "selectionRatio", 0.5),
                Params.dbl(params, "minSelectionRatio", 0.2),
                Params.dbl(params, "maxSelectionRatio", 0.9),
                Params.dbl(params, "memoryDecay", 0.85),
                Params.dbl(params, "targetImprovement", 0.0015),
                Params.dbl(params, "adjustmentStep", 0.015)
        );
    }
}
