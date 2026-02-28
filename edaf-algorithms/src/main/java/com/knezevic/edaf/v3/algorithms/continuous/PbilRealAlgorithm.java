/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated real-coded PBIL driver with distribution updates in continuous space.
 *
 * <p>Typical paired model uses Gaussian marginals whose means follow PBIL-like EMA:
 * <pre>
 *   μ_{t+1} = (1-η) μ_t + η μ_elite
 * </pre>
 * with covariance/scale estimated from elite spread.
 *
 * <p>References:
 * <ol>
 *   <li>M. Sebag and A. Ducoulombier, "Extending population-based incremental learning
 *   to continuous search spaces," PPSN, 1998.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class PbilRealAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new PbilRealAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public PbilRealAlgorithm(double selectionRatio) {
        super("pbil-real", selectionRatio);
    }
}
