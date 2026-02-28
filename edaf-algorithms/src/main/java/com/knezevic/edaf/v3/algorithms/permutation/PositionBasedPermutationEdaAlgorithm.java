/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.permutation;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Dedicated position-based permutation EDA driver.
 *
 * <p>Typical paired model learns position matrix:
 * <pre>
 *   P(item = v at position = i)
 * </pre>
 * and samples permutations with conflict-resolution to maintain validity.
 *
 * <p>References:
 * <ol>
 *   <li>P. Larranaga et al., "Combinatorial optimization by learning and simulation
 *   of Bayesian networks," UAI, 1999.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class PositionBasedPermutationEdaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    /**
     * Creates a new PositionBasedPermutationEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public PositionBasedPermutationEdaAlgorithm(double selectionRatio) {
        super("position-based-permutation-eda", selectionRatio);
    }
}
