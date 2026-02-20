package com.knezevic.edaf.v3.persistence.query;

/**
 * Friedman average rank for one algorithm (lower rank is better).
 */
public record FriedmanRank(
        String algorithm,
        double averageRank
) {
}
