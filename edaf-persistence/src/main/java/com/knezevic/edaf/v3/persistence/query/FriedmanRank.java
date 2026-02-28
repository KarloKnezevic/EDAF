/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * Friedman average rank for one algorithm (lower rank is better).
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record FriedmanRank(
        String algorithm,
        double averageRank
) {
}
