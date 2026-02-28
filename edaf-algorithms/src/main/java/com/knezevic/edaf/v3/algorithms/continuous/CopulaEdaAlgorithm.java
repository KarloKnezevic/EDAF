/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated copula-based EDA driver for non-Gaussian marginals with modeled rank dependence.
 *
 * <p>Typical paired model decomposes density into marginals and copula:
 * <pre>
 *   p(x) = c(F_1(x_1), ..., F_d(x_d)) * Π_d f_d(x_d)
 * </pre>
 * where {@code c} captures cross-dimensional dependence.
 *
 * <p>References:
 * <ol>
 *   <li>R. B. Nelsen, "An Introduction to Copulas," Springer, 2006.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class CopulaEdaAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new CopulaEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public CopulaEdaAlgorithm(double selectionRatio) {
        super("copula-eda", selectionRatio);
    }
}
