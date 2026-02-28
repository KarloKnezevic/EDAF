/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.permutation;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Dedicated EHBSA driver for adjacency-structured permutation optimization.
 *
 * <p>EHBSA-style sampling relies on edge histogram:
 * <pre>
 *   P(next = j | current = i)
 * </pre>
 * estimated from elite adjacency frequencies.
 *
 * <p>References:
 * <ol>
 *   <li>M. Tsutsui, "Probabilistic model-building genetic algorithms in permutation domains,"
 *   GECCO Workshop, 2006.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class EhbsaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    /**
     * Creates a new EhbsaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public EhbsaAlgorithm(double selectionRatio) {
        super("ehbsa", selectionRatio);
    }
}
