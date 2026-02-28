/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

/**
 * Common contract for scalar and vector fitness representations.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface Fitness {

    /**
     * Returns an ordered objective vector view of the fitness.
     *
     * @return objective values
     */
    double[] objectives();

    /**
     * Returns a scalar score used by scalar algorithms.
     *
     * @return scalar fitness value
     */
    double scalar();

    /**
     * Indicates whether this fitness was originally defined as scalar-native.
     *
     * @return true when scalar score is primary value
     */
    boolean scalarNative();
}
