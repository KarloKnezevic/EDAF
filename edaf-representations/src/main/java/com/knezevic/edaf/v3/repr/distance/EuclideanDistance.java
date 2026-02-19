package com.knezevic.edaf.v3.repr.distance;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Euclidean distance utility for real vectors.
 */
public final class EuclideanDistance {

    private EuclideanDistance() {
        // utility class
    }

    public static double between(RealVector a, RealVector b) {
        if (a.length() != b.length()) {
            throw new IllegalArgumentException("Vectors must have same length");
        }
        double sum = 0.0;
        for (int i = 0; i < a.length(); i++) {
            double diff = a.values()[i] - b.values()[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
}
