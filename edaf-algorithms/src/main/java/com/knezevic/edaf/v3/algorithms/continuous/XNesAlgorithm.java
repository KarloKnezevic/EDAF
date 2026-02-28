/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated xNES driver for full-covariance natural-gradient search.
 *
 * <p>xNES updates search distribution in natural coordinates:
 * <pre>
 *   x = μ + A z, z ~ N(0, I), Σ = A A^T
 *   θ_{t+1} = θ_t + η * \tilde{∇}_θ J(θ_t)
 * </pre>
 * where {@code \tilde{∇}} denotes natural gradient.
 *
 * <p>References:
 * <ol>
 *   <li>T. Glasmachers et al., "Exponential natural evolution strategies," GECCO, 2010.</li>
 *   <li>D. Wierstra et al., "Natural evolution strategies," JMLR, 2014.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class XNesAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new XNesAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public XNesAlgorithm(double selectionRatio) {
        super("xnes", selectionRatio);
    }
}
