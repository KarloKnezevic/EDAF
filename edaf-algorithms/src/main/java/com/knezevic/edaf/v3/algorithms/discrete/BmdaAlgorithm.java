/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * BMDA driver
 */
public final class BmdaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    public BmdaAlgorithm(double selectionRatio) {
        super("bmda", selectionRatio);
    }
}
