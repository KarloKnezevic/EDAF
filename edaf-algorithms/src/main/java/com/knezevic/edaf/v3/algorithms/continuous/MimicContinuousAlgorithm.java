/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.continuous;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Dedicated continuous adaptation of MIMIC-style dependency modeling.
 *
 * <p>Continuous MIMIC-style factorization uses an ordered dependency chain:
 * <pre>
 *   p(x) = p(x_{π_1}) Π_{k=2}^{d} p(x_{π_k} | x_{π_{k-1}})
 * </pre>
 * where ordering is learned from elite dependency scores.
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
public final class MimicContinuousAlgorithm extends RatioBasedEdaAlgorithm<RealVector> {

    /**
     * Creates a new MimicContinuousAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public MimicContinuousAlgorithm(double selectionRatio) {
        super("mimic-continuous", selectionRatio);
    }
}
