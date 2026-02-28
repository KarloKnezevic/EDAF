/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated Information-Geometric Optimization (IGO) driver alias.
 *
 * <p>IGO updates distribution parameters along natural gradient:
 * <pre>
 *   θ_{t+1} = θ_t + η I(θ_t)^{-1} ∇_θ J(θ_t)
 * </pre>
 * where {@code I(θ)} is Fisher information matrix.
 *
 * <p>References:
 * <ol>
 *   <li>Y. Ollivier et al., "Information-geometric optimization algorithms:
 *   A unifying picture via invariance principles," JMLR, 2017.</li>
 *   <li>D. Wierstra et al., "Natural evolution strategies," JMLR, 2014.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class IgoAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new IgoAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public IgoAlgorithm(double selectionRatio) {
        super("igo", selectionRatio);
    }
}
