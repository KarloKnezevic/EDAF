package com.knezevic.edaf.v3.problems.crypto.criteria;

import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionStats;

/**
 * One scalar criterion used for boolean-function cryptographic fitness.
 */
public interface CryptoFitnessCriterion {

    /**
     * Stable criterion identifier used in config.
     */
    String id();

    /**
     * Computes normalized score where higher is better.
     */
    double score(BooleanFunctionStats stats);
}
