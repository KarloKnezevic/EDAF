/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated KDE-EDA driver using non-parametric density estimation.
 *
 * <p>Typical paired model estimates density from elite kernels:
 * <pre>
 *   \hat{p}(x) = (1 / (n h^d)) Σ_{i=1}^{n} K((x-x_i)/h)
 * </pre>
 * and samples by perturbing elite points with bandwidth-scaled noise.
 *
 * <p>References:
 * <ol>
 *   <li>B. W. Silverman, "Density Estimation for Statistics and Data Analysis," 1986.</li>
 *   <li>N. Luo and F. Qian, "Evolutionary algorithm using kernel density estimation
 *   model in continuous domain," ASCC, 2009.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class KdeEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new KdeEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public KdeEdaAlgorithm(double selectionRatio) {
        super("kde-eda", selectionRatio);
    }
}
