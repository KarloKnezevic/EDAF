/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.mo;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

/**
 * Indicator-based EDA driver
 */
public final class IndicatorEdaAlgorithm extends RatioBasedEdaAlgorithm<Object> {

    public IndicatorEdaAlgorithm(double selectionRatio) {
        super("indicator-eda", selectionRatio);
    }
}
