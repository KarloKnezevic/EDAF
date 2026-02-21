package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Dependency-tree EDA driver
 */
public final class DependencyTreeEdaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    public DependencyTreeEdaAlgorithm(double selectionRatio) {
        super("dependency-tree-eda", selectionRatio);
    }
}
