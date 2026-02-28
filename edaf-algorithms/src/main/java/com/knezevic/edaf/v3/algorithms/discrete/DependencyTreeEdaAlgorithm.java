/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Dedicated dependency-tree EDA driver (alias for tree-structured discrete modeling).
 *
 * <p>Typical paired model applies dependency-tree factorization:
 * <pre>
 *   p(x) = p(x_r) Π_i p(x_i | x_parent(i))
 * </pre>
 * where tree structure is inferred from elite pairwise dependencies.
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
public final class DependencyTreeEdaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    /**
     * Creates a new DependencyTreeEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public DependencyTreeEdaAlgorithm(double selectionRatio) {
        super("dependency-tree-eda", selectionRatio);
    }
}
