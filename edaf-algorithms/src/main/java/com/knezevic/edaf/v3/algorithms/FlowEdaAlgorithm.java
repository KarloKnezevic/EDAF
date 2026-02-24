/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Flow-based continuous EDA driver.
 */
public final class FlowEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    public FlowEdaAlgorithm(double selectionRatio) {
        super("flow-eda", selectionRatio);
    }
}
