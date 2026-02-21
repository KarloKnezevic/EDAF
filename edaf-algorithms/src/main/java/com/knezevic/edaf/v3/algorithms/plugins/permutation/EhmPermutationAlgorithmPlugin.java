package com.knezevic.edaf.v3.algorithms.plugins.permutation;

import com.knezevic.edaf.v3.algorithms.EhmPermutationEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
 * Plugin for EHM permutation EDA algorithm.
 */
public final class EhmPermutationAlgorithmPlugin implements AlgorithmPlugin<PermutationVector> {

    @Override
    public String type() {
        return "ehm-eda";
    }

    @Override
    public String description() {
        return "Edge Histogram permutation EDA driver";
    }

    @Override
    public Algorithm<PermutationVector> create(AlgorithmDependencies<PermutationVector> dependencies, Map<String, Object> params) {
        return new EhmPermutationEdaAlgorithm(Params.dbl(params, "selectionRatio", 0.4));
    }
}
