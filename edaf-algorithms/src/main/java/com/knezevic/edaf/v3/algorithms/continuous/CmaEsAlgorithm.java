/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated CMA-ES strategy driver for full-covariance adaptive search.
 *
 * <p>CMA-ES updates mean, step size, and covariance:
 * <pre>
 *   m_{t+1} = m_t + Σ_i w_i (x_i - m_t)
 *   σ_{t+1} = σ_t * exp(...)
 *   C_{t+1} = (1-c_1-c_μ)C_t + c_1 p_c p_c^T + c_μ Σ_i w_i y_i y_i^T
 * </pre>
 * where {@code y_i = (x_i - m_t) / σ_t}.
 *
 * <p>References:
 * <ol>
 *   <li>N. Hansen and A. Ostermeier, "Completely derandomized self-adaptation in
 *   evolution strategies," Evolutionary Computation, 2001.</li>
 *   <li>N. Hansen, "The CMA Evolution Strategy: A Tutorial," arXiv:1604.00772, 2016.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class CmaEsAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new CmaEsAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public CmaEsAlgorithm(double selectionRatio) {
        super("cma-es", selectionRatio);
    }
}
