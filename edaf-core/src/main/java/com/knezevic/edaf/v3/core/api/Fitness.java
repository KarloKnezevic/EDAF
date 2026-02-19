package com.knezevic.edaf.v3.core.api;

/**
 * Common contract for scalar and vector fitness representations.
 */
public interface Fitness {

    /**
     * Returns an ordered objective vector view of the fitness.
     */
    double[] objectives();

    /**
     * Returns a scalar score used by scalar algorithms.
     */
    double scalar();

    /**
     * Indicates whether this fitness was originally defined as a scalar.
     */
    boolean scalarNative();
}
