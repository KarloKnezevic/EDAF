/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.permutation;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Mallows EDA driver
 */
public final class MallowsEdaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    public MallowsEdaAlgorithm(double selectionRatio) {
        super("mallows-eda", selectionRatio);
    }
}
