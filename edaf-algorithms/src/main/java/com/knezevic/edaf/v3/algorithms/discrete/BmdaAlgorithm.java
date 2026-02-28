/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Dedicated BMDA driver that typically pairs with tree-based bivariate dependency models.
 *
 * <p>Typical paired model uses bivariate tree factorization:
 * <pre>
 *   p(x) = p(x_r) Π_i p(x_i | x_parent(i))
 * </pre>
 * where dependencies are estimated from elite mutual information.
 *
 * <p>References:
 * <ol>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 *   <li>C. K. Chow and C. N. Liu, "Approximating discrete probability distributions
 *   with dependence trees," IEEE Transactions on Information Theory, 1968.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BmdaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    /**
     * Creates a new BmdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public BmdaAlgorithm(double selectionRatio) {
        super("bmda", selectionRatio);
    }
}
