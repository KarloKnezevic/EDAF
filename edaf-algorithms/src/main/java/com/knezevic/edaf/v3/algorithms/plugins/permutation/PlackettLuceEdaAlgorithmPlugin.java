package com.knezevic.edaf.v3.algorithms.plugins.permutation;

import com.knezevic.edaf.v3.algorithms.permutation.PlackettLuceEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
 * Plugin for Plackett-Luce EDA driver
 */
public final class PlackettLuceEdaAlgorithmPlugin implements AlgorithmPlugin<PermutationVector> {

    @Override
    public String type() {
        return "plackett-luce-eda";
    }

    @Override
    public String description() {
        return "Plackett-Luce EDA driver";
    }

    @Override
    public Algorithm<PermutationVector> create(AlgorithmDependencies<PermutationVector> dependencies, Map<String, Object> params) {
        return new PlackettLuceEdaAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
