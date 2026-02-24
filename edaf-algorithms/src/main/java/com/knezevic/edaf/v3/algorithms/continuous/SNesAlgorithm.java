/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Separable NES driver
 */
public final class SNesAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public SNesAlgorithm(double selectionRatio) {
        super("snes", selectionRatio);
    }
}
