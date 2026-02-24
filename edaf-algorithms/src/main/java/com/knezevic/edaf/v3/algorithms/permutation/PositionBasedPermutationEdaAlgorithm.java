/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.permutation;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Position-based permutation EDA driver.
 */
public final class PositionBasedPermutationEdaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    public PositionBasedPermutationEdaAlgorithm(double selectionRatio) {
        super("position-based-permutation-eda", selectionRatio);
    }
}
