/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Dedicated factorized-discrete EDA driver for independent-locus probability models.
 *
 * <p>Typical paired factorized model:
 * <pre>
 *   p(x) = Π_i p_i(x_i)
 * </pre>
 * which ignores cross-locus dependencies and optimizes via fast univariate updates.
 *
 * <p>References:
 * <ol>
 *   <li>H. Muehlenbein and G. Paass, "From recombination of genes to the estimation of
 *   distributions I. Binary parameters," PPSN IV, 1996.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class FactorizedDiscreteEdaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    /**
     * Creates a new FactorizedDiscreteEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public FactorizedDiscreteEdaAlgorithm(double selectionRatio) {
        super("factorized-discrete-eda", selectionRatio);
    }
}
