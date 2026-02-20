package com.knezevic.edaf.v3.algorithms.tree;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

/**
 * Ratio-based EDA driver for variable-length token tree representations.
 */
public final class TreeEdaAlgorithm extends RatioBasedEdaAlgorithm<VariableLengthVector<Integer>> {

    public TreeEdaAlgorithm(double selectionRatio) {
        super("tree-eda", selectionRatio);
    }
}
