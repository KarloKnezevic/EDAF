/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated block-covariance continuous EDA driver for partially factorized dependence structures.
 *
 * <p>Typical paired model factorizes covariance into blocks:
 * <pre>
 *   Σ ≈ blockdiag(Σ_1, Σ_2, ..., Σ_B)
 * </pre>
 * balancing expressiveness and sampling cost in high-dimensional search.
 *
 * <p>References:
 * <ol>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 *   <li>N. Hansen, "The CMA Evolution Strategy: A Tutorial," arXiv:1604.00772, 2016.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BlockCovarianceEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new BlockCovarianceEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public BlockCovarianceEdaAlgorithm(double selectionRatio) {
        super("block-covariance-eda", selectionRatio);
    }
}
