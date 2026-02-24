/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Copula EDA driver
 */
public final class CopulaEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public CopulaEdaAlgorithm(double selectionRatio) {
        super("copula-eda", selectionRatio);
    }
}
