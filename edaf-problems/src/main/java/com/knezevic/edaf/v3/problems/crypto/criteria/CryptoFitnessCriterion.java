/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.crypto.criteria;

import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionStats;

/**
 * One scalar criterion used for boolean-function cryptographic fitness.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
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
