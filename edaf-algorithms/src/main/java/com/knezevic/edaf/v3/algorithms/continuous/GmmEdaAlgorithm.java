/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * GMM-EDA driver
 */
public final class GmmEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public GmmEdaAlgorithm(double selectionRatio) {
        super("gmm-eda", selectionRatio);
    }
}
