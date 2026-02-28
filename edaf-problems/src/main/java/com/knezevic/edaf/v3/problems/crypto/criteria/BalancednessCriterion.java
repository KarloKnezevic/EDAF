/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.crypto.criteria;

import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionStats;

/**
 * Measures closeness to strict balancedness.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BalancednessCriterion implements CryptoFitnessCriterion {

    /**
     * Returns identifier.
     *
     * @return identifier value
     */
    @Override
    public String id() {
        return "balancedness";
    }

    /**
     * Executes score.
     *
     * @param stats the stats argument
     * @return the computed score
     */
    @Override
    public double score(BooleanFunctionStats stats) {
        double target = stats.size() / 2.0;
        double deviation = Math.abs(stats.ones() - target);
        if (target <= 0.0) {
            return 0.0;
        }
        return Math.max(0.0, 1.0 - deviation / target);
    }
}
