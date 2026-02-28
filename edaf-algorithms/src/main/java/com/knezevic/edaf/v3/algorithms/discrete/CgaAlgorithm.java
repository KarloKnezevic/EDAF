/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Dedicated compact-GA style driver with ratio-based elite selection.
 *
 * <p>Typical cGA-style update on probability vector is:
 * <pre>
 *   p_i^{t+1} = p_i^t + (1/K) * sign(x_i^{winner} - x_i^{loser})
 * </pre>
 * with clipping away from exact zero/one to avoid fixation artifacts.
 *
 * <p>References:
 * <ol>
 *   <li>G. R. Harik, F. G. Lobo, and D. E. Goldberg, "The compact genetic algorithm,"
 *   IEEE Transactions on Evolutionary Computation, 1999.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class CgaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    /**
     * Creates a new CgaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public CgaAlgorithm(double selectionRatio) {
        super("cga", selectionRatio);
    }
}
