package com.knezevic.edaf.v3.algorithms.plugins.permutation;

import com.knezevic.edaf.v3.algorithms.permutation.EhbsaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
 * Plugin for Edge Histogram Based Sampling Algorithm.
 */
public final class EhbsaAlgorithmPlugin implements AlgorithmPlugin<PermutationVector> {

    @Override
    public String type() {
        return "ehbsa";
    }

    @Override
    public String description() {
        return "Edge Histogram Based Sampling Algorithm driver";
    }

    @Override
    public Algorithm<PermutationVector> create(AlgorithmDependencies<PermutationVector> dependencies,
                                               Map<String, Object> params) {
        return new EhbsaAlgorithm(Params.dbl(params, "selectionRatio", 0.4));
    }
}
