package com.knezevic.edaf.v3.algorithms.plugins.permutation;

import com.knezevic.edaf.v3.algorithms.permutation.KendallPermutationEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
 * Plugin for Kendall-distance permutation EDA.
 */
public final class KendallPermutationEdaAlgorithmPlugin implements AlgorithmPlugin<PermutationVector> {

    @Override
    public String type() {
        return "kendall-permutation-eda";
    }

    @Override
    public String description() {
        return "Kendall-distance permutation EDA driver";
    }

    @Override
    public Algorithm<PermutationVector> create(AlgorithmDependencies<PermutationVector> dependencies,
                                               Map<String, Object> params) {
        return new KendallPermutationEdaAlgorithm(Params.dbl(params, "selectionRatio", 0.4));
    }
}
