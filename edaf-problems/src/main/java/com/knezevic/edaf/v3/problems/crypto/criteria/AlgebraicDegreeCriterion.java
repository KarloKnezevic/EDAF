package com.knezevic.edaf.v3.problems.crypto.criteria;

import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionStats;

/**
 * Normalized algebraic degree score based on ANF transform.
 */
public final class AlgebraicDegreeCriterion implements CryptoFitnessCriterion {

    @Override
    public String id() {
        return "algebraic-degree";
    }

    @Override
    public double score(BooleanFunctionStats stats) {
        if (stats.n() <= 0) {
            return 0.0;
        }
        return stats.algebraicDegree() / (double) stats.n();
    }
}
