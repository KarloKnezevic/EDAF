/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated Cross-Entropy Method (CEM) continuous driver.
 *
 * <p>CEM updates distribution parameters by elite maximum-likelihood:
 * <pre>
 *   θ_{t+1} = argmax_θ Σ_{x in elite_t} log p(x; θ)
 * </pre>
 * with elite set defined by top {@code selectionRatio} quantile.
 *
 * <p>References:
 * <ol>
 *   <li>R. Rubinstein and D. Kroese, "The Cross-Entropy Method," Springer, 2004.</li>
 *   <li>P.-T. de Boer et al., "A tutorial on the cross-entropy method," Annals of
 *   Operations Research, 2005.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class CemAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new CemAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public CemAlgorithm(double selectionRatio) {
        super("cem", selectionRatio);
    }
}
