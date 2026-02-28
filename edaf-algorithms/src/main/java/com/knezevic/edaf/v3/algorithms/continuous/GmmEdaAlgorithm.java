/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated Gaussian-mixture EDA driver for multimodal continuous landscapes.
 *
 * <p>Typical paired model uses finite Gaussian mixture:
 * <pre>
 *   p(x) = Σ_{k=1}^{K} π_k N(x; μ_k, Σ_k)
 * </pre>
 * and updates parameters with EM on elites.
 *
 * <p>References:
 * <ol>
 *   <li>A. P. Dempster, N. M. Laird, and D. B. Rubin, "Maximum likelihood from
 *   incomplete data via the EM algorithm," JRSS B, 1977.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class GmmEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new GmmEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public GmmEdaAlgorithm(double selectionRatio) {
        super("gmm-eda", selectionRatio);
    }
}
