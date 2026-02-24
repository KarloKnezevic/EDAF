/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * MIMIC driver
 */
public final class MimicAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    public MimicAlgorithm(double selectionRatio) {
        super("mimic", selectionRatio);
    }
}
