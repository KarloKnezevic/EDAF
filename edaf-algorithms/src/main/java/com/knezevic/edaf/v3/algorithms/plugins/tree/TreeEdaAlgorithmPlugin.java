package com.knezevic.edaf.v3.algorithms.plugins.tree;

import com.knezevic.edaf.v3.algorithms.tree.TreeEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.util.Map;

/**
 * Plugin for tree EDA driver.
 */
public final class TreeEdaAlgorithmPlugin implements AlgorithmPlugin<VariableLengthVector<Integer>> {

    @Override
    public String type() {
        return "tree-eda";
    }

    @Override
    public String description() {
        return "Tree EDA driver";
    }

    @Override
    public Algorithm<VariableLengthVector<Integer>> create(AlgorithmDependencies<VariableLengthVector<Integer>> dependencies,
                                                           Map<String, Object> params) {
        double selectionRatio = Params.dbl(params, "selectionRatio", 0.5);
        return new TreeEdaAlgorithm(selectionRatio);
    }
}
