/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Dedicated EHM/EHBSA-style permutation driver based on edge-histogram modeling.
 *
 * <p>Typical paired model represents adjacency probabilities:
 * <pre>
 *   P(next = j | current = i)
 * </pre>
 * and samples permutations by traversing unused nodes according to transition rows.
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
public final class EhmPermutationEdaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    /**
     * Creates a new EhmPermutationEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public EhmPermutationEdaAlgorithm(double selectionRatio) {
        super("ehm-eda", selectionRatio);
    }
}
