/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated continuous UMDA-style driver with independent Gaussian marginals.
 *
 * <p>Typical paired model assumes independent Gaussian coordinates:
 * <pre>
 *   p(x) = Π_d N(x_d; μ_d, σ_d²)
 * </pre>
 * where {@code μ_d, σ_d} are estimated from elite samples only.
 *
 * <p>References:
 * <ol>
 *   <li>H. Muehlenbein, J. Bendisch, and H.-M. Voigt, "From recombination of genes to
 *   the estimation of distributions II. Continuous parameters," PPSN IV, 1996.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class UmdaContinuousAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new UmdaContinuousAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public UmdaContinuousAlgorithm(double selectionRatio) {
        super("umda-continuous", selectionRatio);
    }
}
