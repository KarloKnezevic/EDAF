/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.mo;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

/**
 * Indicator-based multi-objective EDA driver alias.
 *
 * <p>Selection maximizes indicator contribution before model fitting:
 * <pre>
 *   S_t = argmax_{|S|=μ} I(S),   θ_{t+1} = Fit(S_t)
 * </pre>
 * where {@code I} can be hypervolume-like or epsilon-indicator score.
 *
 * <p>References:
 * <ol>
 *   <li>E. Zitzler and S. Künzli, "Indicator-based selection in multiobjective search,"
 *   PPSN, 2004.</li>
 *   <li>N. Beume et al., "SMS-EMOA: Multiobjective selection based on dominated hypervolume,"
 *   EJOR, 2007.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class IndicatorEdaAlgorithm extends RatioBasedEdaAlgorithm<Object> {

    /**
     * Creates a new IndicatorEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public IndicatorEdaAlgorithm(double selectionRatio) {
        super("indicator-eda", selectionRatio);
    }
}
