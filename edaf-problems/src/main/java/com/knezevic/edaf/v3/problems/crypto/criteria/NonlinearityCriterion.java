/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.crypto.criteria;

import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionStats;

/**
 * Normalized Walsh-spectrum nonlinearity score.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class NonlinearityCriterion implements CryptoFitnessCriterion {

    /**
     * Returns identifier.
     *
     * @return identifier value
     */
    @Override
    public String id() {
        return "nonlinearity";
    }

    /**
     * Executes score.
     *
     * @param stats the stats argument
     * @return the computed score
     */
    @Override
    public double score(BooleanFunctionStats stats) {
        double upper = stats.nonlinearityUpperBound();
        if (upper <= 0.0) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, stats.nonlinearity() / upper));
    }
}
