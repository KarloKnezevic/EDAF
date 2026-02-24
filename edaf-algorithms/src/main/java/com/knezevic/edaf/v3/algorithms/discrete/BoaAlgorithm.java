/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Bayesian Optimization Algorithm driver
 */
public final class BoaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    public BoaAlgorithm(double selectionRatio) {
        super("boa", selectionRatio);
    }
}
