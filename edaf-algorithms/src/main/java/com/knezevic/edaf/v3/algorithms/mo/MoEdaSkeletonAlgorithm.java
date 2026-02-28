/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.mo;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

/**
 * Baseline multi-objective EDA driver using scalarized selection.
 *
 * <p>Baseline scalarization can be written as:
 * <pre>
 *   f_λ(x) = Σ_{m=1}^{M} λ_m f_m(x),   Σ_m λ_m = 1
 * </pre>
 * and standard EDA update is applied on selected elites under {@code f_λ}.
 *
 * <p>References:
 * <ol>
 *   <li>K. Miettinen, "Nonlinear Multiobjective Optimization," Kluwer, 1999.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms,"
 *   Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MoEdaSkeletonAlgorithm<G> extends RatioBasedEdaAlgorithm<G> {

    /**
     * Creates a new MoEdaSkeletonAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public MoEdaSkeletonAlgorithm(double selectionRatio) {
        super("mo-eda-skeleton", selectionRatio);
    }
}
