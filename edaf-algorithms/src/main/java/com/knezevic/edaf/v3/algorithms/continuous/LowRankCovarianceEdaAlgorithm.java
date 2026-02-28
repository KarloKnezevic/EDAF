/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated low-rank covariance EDA driver for high-dimensional continuous optimization.
 *
 * <p>Typical paired model approximates covariance as:
 * <pre>
 *   Σ ≈ U U^T + diag(v)
 * </pre>
 * where {@code U in R^{d x r}} captures dominant directions and {@code v} keeps
 * per-dimension residual variance.
 *
 * <p>References:
 * <ol>
 *   <li>R. Ros and N. Hansen, "A simple modification in CMA-ES achieving linear time and
 *   space complexity," PPSN, 2008.</li>
 *   <li>I. Loshchilov, "A computationally efficient limited memory CMA-ES for large scale
 *   optimization," GECCO, 2014.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class LowRankCovarianceEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new LowRankCovarianceEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public LowRankCovarianceEdaAlgorithm(double selectionRatio) {
        super("lowrank-covariance-eda", selectionRatio);
    }
}
