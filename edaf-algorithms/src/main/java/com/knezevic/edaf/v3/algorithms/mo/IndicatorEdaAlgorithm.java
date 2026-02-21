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
