/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.permutation;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Dedicated Mallows-model permutation EDA driver.
 *
 * <p>Mallows distribution under Kendall distance:
 * <pre>
 *   p(π | σ, φ) = Z(φ)^{-1} φ^{K(π,σ)}
 * </pre>
 * where {@code σ} is consensus ranking estimated from elites.
 *
 * <p>References:
 * <ol>
 *   <li>C. L. Mallows, "Non-null ranking models. I," Biometrika, 1957.</li>
 *   <li>P. Fligner and J. Verducci, "Distance based ranking models," JRSS B, 1986.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MallowsEdaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    /**
     * Creates a new MallowsEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public MallowsEdaAlgorithm(double selectionRatio) {
        super("mallows-eda", selectionRatio);
    }
}
