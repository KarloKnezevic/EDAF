/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated full-covariance Gaussian EDA driver for correlated real-valued search.
 *
 * <p>Typical paired model learns correlated Gaussian density:
 * <pre>
 *   p(x) = N(x; μ, Σ)
 * </pre>
 * and samples via Cholesky factorization {@code x = μ + Lz}, {@code LL^T = Σ}.
 *
 * <p>References:
 * <ol>
 *   <li>P. Larranaga, "Optimization in continuous domains by learning and simulation
 *   of Gaussian networks," GECCO Workshop, 2000.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class FullCovarianceEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new FullCovarianceEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public FullCovarianceEdaAlgorithm(double selectionRatio) {
        super("full-covariance-eda", selectionRatio);
    }
}
