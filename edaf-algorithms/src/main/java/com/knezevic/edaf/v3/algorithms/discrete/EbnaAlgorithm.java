/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Dedicated EBNA driver for Bayesian-network estimation of categorical distributions.
 *
 * <p>Typical paired model learns Bayesian network by maximizing decomposable score:
 * <pre>
 *   p(x) = Π_i p(x_i | Pa_i)
 * </pre>
 * with structure learned from elite sample statistics.
 *
 * <p>References:
 * <ol>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 *   <li>E. Etxeberria and P. Larranaga, "Global optimization using Bayesian networks,"
 *   in EDA literature compendium, 1999.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class EbnaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    /**
     * Creates a new EbnaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public EbnaAlgorithm(double selectionRatio) {
        super("ebna", selectionRatio);
    }
}
