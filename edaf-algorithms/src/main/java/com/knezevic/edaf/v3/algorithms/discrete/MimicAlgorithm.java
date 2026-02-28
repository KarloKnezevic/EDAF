/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.discrete;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Dedicated MIMIC driver for dependency-aware binary modeling.
 *
 * <p>MIMIC-style ordered factorization:
 * <pre>
 *   p(x) = p(x_{π_1}) Π_{k=2}^{d} p(x_{π_k} | x_{π_{k-1}})
 * </pre>
 * where ordering {@code π} is learned to maximize information retention.
 *
 * <p>References:
 * <ol>
 *   <li>J. S. de Bonet, C. Isbell, and P. Viola, "MIMIC: Finding optima by estimating
 *   probability densities," NIPS, 1997.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MimicAlgorithm extends RatioBasedEdaAlgorithm<BitString> {

    /**
     * Creates a new MimicAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public MimicAlgorithm(double selectionRatio) {
        super("mimic", selectionRatio);
    }
}
