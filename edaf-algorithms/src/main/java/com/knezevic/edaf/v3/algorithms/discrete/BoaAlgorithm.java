/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Dedicated BOA driver for Bayesian-network-based discrete EDAs.
 *
 * <p>Typical paired model learns Bayesian network over elite individuals:
 * <pre>
 *   p(x) = Π_i p(x_i | Pa_i)
 * </pre>
 * where parent sets are selected by structure score (e.g., BDe/BIC variants).
 *
 * <p>References:
 * <ol>
 *   <li>M. Pelikan, D. E. Goldberg, and E. Cantú-Paz, "BOA," GECCO, 1999.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BoaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    /**
     * Creates a new BoaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public BoaAlgorithm(double selectionRatio) {
        super("boa", selectionRatio);
    }
}
