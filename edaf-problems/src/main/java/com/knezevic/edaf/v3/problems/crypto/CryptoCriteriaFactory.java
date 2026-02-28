/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.crypto;

import com.knezevic.edaf.v3.problems.crypto.criteria.AlgebraicDegreeCriterion;
import com.knezevic.edaf.v3.problems.crypto.criteria.BalancednessCriterion;
import com.knezevic.edaf.v3.problems.crypto.criteria.CryptoFitnessCriterion;
import com.knezevic.edaf.v3.problems.crypto.criteria.NonlinearityCriterion;

import java.util.Locale;

/**
 * Factory for configured cryptographic boolean-function criteria.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class CryptoCriteriaFactory {

    private CryptoCriteriaFactory() {
        // utility class
    }

    /**
     * Creates criterion by id with backwards-compatible aliases.
     * @param rawId raw criterion identifier
     * @return component instance
     */
    public static CryptoFitnessCriterion create(String rawId) {
        String id = rawId == null ? "" : rawId.trim().toLowerCase(Locale.ROOT);
        return switch (id) {
            case "balancedness", "balance", "balanced" -> new BalancednessCriterion();
            case "nonlinearity", "nl" -> new NonlinearityCriterion();
            case "algebraicdegree", "algebraic-degree", "degree" -> new AlgebraicDegreeCriterion();
            default -> throw new IllegalArgumentException(
                    "Unknown crypto criterion '" + rawId + "'. Supported: balancedness, nonlinearity, algebraic-degree"
            );
        };
    }
}
