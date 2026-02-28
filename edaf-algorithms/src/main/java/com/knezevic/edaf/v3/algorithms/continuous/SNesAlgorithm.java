/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated separable NES (sNES) driver.
 *
 * <p>sNES uses separable natural-gradient update:
 * <pre>
 *   ∇_μ ∝ Σ_k u_k z_k,   ∇_σ ∝ Σ_k u_k (z_k² - 1)
 * </pre>
 * with sampling {@code x = μ + σ ⊙ z}, {@code z ~ N(0, I)}.
 *
 * <p>References:
 * <ol>
 *   <li>T. Schaul, T. Glasmachers, and J. Schmidhuber, "High dimensions and heavy tails
 *   for natural evolution strategies," GECCO, 2011.</li>
 *   <li>D. Wierstra et al., "Natural evolution strategies," JMLR, 2014.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class SNesAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new SNesAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public SNesAlgorithm(double selectionRatio) {
        super("snes", selectionRatio);
    }
}
