/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Dedicated hierarchical BOA (hBOA) driver for binary dependency-heavy search.
 *
 * <p>Typical paired model is a sparse Bayesian network with conditional factorization:
 * <pre>
 *   p(x) = Π_i p(x_i | Pa_i)
 * </pre>
 * with hierarchical decomposition of variable interactions.
 *
 * <p>References:
 * <ol>
 *   <li>M. Pelikan and D. E. Goldberg, "Hierarchical Bayesian optimization algorithm,"
 *   GECCO, 2001.</li>
 *   <li>M. Pelikan, D. E. Goldberg, and E. Cantú-Paz, "BOA," GECCO, 1999.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class HBoaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    /**
     * Creates a new HBoaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public HBoaAlgorithm(double selectionRatio) {
        super("hboa", selectionRatio);
    }
}
