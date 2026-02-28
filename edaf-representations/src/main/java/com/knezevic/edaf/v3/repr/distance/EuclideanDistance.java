/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.distance;

import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Euclidean distance utility for real vectors.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class EuclideanDistance {

    private EuclideanDistance() {
        // utility class
    }

    /**
     * Computes Euclidean distance between two real-valued vectors.
     *
     * @param a first vector
     * @param b second vector
     * @return the Euclidean distance between {@code a} and {@code b}
     */
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
