/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Dedicated UMDA discrete driver.
 *
 * <p>Typical paired model is a factorized Bernoulli distribution:
 * <pre>
 *   p(x) = Π_i Bernoulli(x_i; p_i),   p_i = mean_elite(x_i)
 * </pre>
 * where elite size is {@code μ = round(selectionRatio * N)}.
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
public final class UmdaAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    /**
     * Creates a new UmdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public UmdaAlgorithm(double selectionRatio) {
        super("umda", selectionRatio);
    }
}
