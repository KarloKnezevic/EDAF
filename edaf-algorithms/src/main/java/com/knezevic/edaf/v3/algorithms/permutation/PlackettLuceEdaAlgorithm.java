/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.permutation;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Dedicated Plackett-Luce permutation EDA driver.
 *
 * <p>Plackett-Luce ranking probability:
 * <pre>
 *   P(π) = Π_{k=1}^{n} w_{π_k} / Σ_{j=k}^{n} w_{π_j}
 * </pre>
 * with item weights estimated from elite position statistics.
 *
 * <p>References:
 * <ol>
 *   <li>R. D. Luce, "Individual Choice Behavior," Wiley, 1959.</li>
 *   <li>R. L. Plackett, "The analysis of permutations," Applied Statistics, 1975.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class PlackettLuceEdaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    /**
     * Creates a new PlackettLuceEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public PlackettLuceEdaAlgorithm(double selectionRatio) {
        super("plackett-luce-eda", selectionRatio);
    }
}
