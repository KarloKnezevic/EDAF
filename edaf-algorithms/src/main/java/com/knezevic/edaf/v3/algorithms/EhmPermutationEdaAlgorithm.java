/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * EHM permutation EDA driver.
 */
public final class EhmPermutationEdaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    public EhmPermutationEdaAlgorithm(double selectionRatio) {
        super("ehm-eda", selectionRatio);
    }
}
