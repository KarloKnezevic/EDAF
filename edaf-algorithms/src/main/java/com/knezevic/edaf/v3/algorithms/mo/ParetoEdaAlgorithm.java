/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.mo;

import com.knezevic.edaf.v3.algorithms.RatioBasedEdaAlgorithm;

/**
 * Pareto-based multi-objective EDA driver alias.
 *
 * <p>Selection is based on non-dominated archive:
 * <pre>
 *   A_t = ND(P_t),   θ_{t+1} = Fit(A_t)
 * </pre>
 * where {@code ND} returns Pareto non-dominated set.
 *
 * <p>References:
 * <ol>
 *   <li>K. Deb et al., "A fast and elitist multiobjective genetic algorithm: NSGA-II,"
 *   IEEE Transactions on Evolutionary Computation, 2002.</li>
 *   <li>P. A. N. Bosman and D. Thierens, "The balance between proximity and diversity in
 *   multiobjective evolutionary algorithms," TEC, 2003.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class ParetoEdaAlgorithm extends RatioBasedEdaAlgorithm<Object> {

    /**
     * Creates a new ParetoEdaAlgorithm instance.
     *
     * @param selectionRatio fraction of population used for model fitting
     */
    public ParetoEdaAlgorithm(double selectionRatio) {
        super("pareto-eda", selectionRatio);
    }
}
