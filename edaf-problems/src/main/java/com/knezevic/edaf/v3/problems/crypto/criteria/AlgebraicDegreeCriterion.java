/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.crypto.criteria;

import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionStats;

/**
 * Normalized algebraic degree score based on ANF transform.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class AlgebraicDegreeCriterion implements CryptoFitnessCriterion {

    /**
     * Returns identifier.
     *
     * @return identifier value
     */
    @Override
    public String id() {
        return "algebraic-degree";
    }

    /**
     * Executes score.
     *
     * @param stats the stats argument
     * @return the computed score
     */
    @Override
    public double score(BooleanFunctionStats stats) {
        if (stats.n() <= 0) {
            return 0.0;
        }
        return stats.algebraicDegree() / (double) stats.n();
    }
}
