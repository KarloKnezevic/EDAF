/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.mo;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

/**
 * Pareto-based EDA driver
 */
public final class ParetoEdaAlgorithm extends RatioBasedEdaAlgorithm<Object> {

    public ParetoEdaAlgorithm(double selectionRatio) {
        super("pareto-eda", selectionRatio);
    }
}
