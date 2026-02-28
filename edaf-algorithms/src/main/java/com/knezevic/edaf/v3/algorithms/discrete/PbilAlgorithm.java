/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Dedicated PBIL driver for probability-vector style discrete optimization.
 *
 * <p>Typical PBIL update for each locus:
 * <pre>
 *   p_i^{t+1} = (1-η) p_i^t + η * mean_elite(x_i)
 * </pre>
 * with optional mutation/noise variants in extended implementations.
 *
 * <p>References:
 * <ol>
 *   <li>S. Baluja, "Population-Based Incremental Learning: A Method for Integrating
 *   Genetic Search Based Function Optimization and Competitive Learning," 1994.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class PbilAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    /**
     * Creates a new PbilAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public PbilAlgorithm(double selectionRatio) {
        super("pbil", selectionRatio);
    }
}
