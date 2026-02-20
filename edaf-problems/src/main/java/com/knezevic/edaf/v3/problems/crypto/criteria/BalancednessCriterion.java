package com.knezevic.edaf.v3.problems.crypto.criteria;

import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionStats;

/**
 * Measures closeness to strict balancedness.
 */
public final class BalancednessCriterion implements CryptoFitnessCriterion {

    @Override
    public String id() {
        return "balancedness";
    }

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
