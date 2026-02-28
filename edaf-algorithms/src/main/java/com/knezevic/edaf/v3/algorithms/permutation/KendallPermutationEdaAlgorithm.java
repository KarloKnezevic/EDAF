/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.permutation;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.PermutationVector;

/**
 * Dedicated Kendall-distance permutation EDA driver alias.
 *
 * <p>Typical paired model (Mallows/Kendall family):
 * <pre>
 *   p(π | σ, φ) ∝ φ^{K(π,σ)}
 * </pre>
 * where {@code K(π,σ)} is Kendall tau distance to consensus permutation {@code σ}.
 *
 * <p>References:
 * <ol>
 *   <li>C. L. Mallows, "Non-null ranking models. I," Biometrika, 1957.</li>
 *   <li>F. Critchlow, "Metric Methods for Analyzing Partially Ranked Data," 1985.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class KendallPermutationEdaAlgorithm extends RatioBasedEdaAlgorithm<PermutationVector> {

    /**
     * Creates a new KendallPermutationEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public KendallPermutationEdaAlgorithm(double selectionRatio) {
        super("kendall-permutation-eda", selectionRatio);
    }
}
