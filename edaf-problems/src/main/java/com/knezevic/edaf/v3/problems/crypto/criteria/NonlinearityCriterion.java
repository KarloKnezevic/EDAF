package com.knezevic.edaf.v3.problems.crypto.criteria;

import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionStats;

/**
 * Normalized Walsh-spectrum nonlinearity score.
 */
public final class NonlinearityCriterion implements CryptoFitnessCriterion {

    @Override
    public String id() {
        return "nonlinearity";
    }

    @Override
    public double score(BooleanFunctionStats stats) {
        double upper = stats.nonlinearityUpperBound();
        if (upper <= 0.0) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, stats.nonlinearity() / upper));
    }
}
