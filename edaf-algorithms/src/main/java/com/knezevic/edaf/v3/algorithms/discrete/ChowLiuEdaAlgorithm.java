/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Dedicated Chow-Liu dependency-tree EDA driver for binary/categorical search.
 *
 * <p>Typical paired model fits maximum-weight spanning tree by mutual information:
 * <pre>
 *   T* = argmax_T Σ_{(i,j) in T} I(X_i; X_j)
 * </pre>
 * then factorizes distribution along tree edges.
 *
 * <p>References:
 * <ol>
 *   <li>C. K. Chow and C. N. Liu, "Approximating discrete probability distributions
 *   with dependence trees," IEEE Transactions on Information Theory, 1968.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class ChowLiuEdaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    /**
     * Creates a new ChowLiuEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public ChowLiuEdaAlgorithm(double selectionRatio) {
        super("chow-liu-eda", selectionRatio);
    }
}
