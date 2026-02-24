/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

/**
 * Defines when an algorithm run should stop.
 */
public interface StoppingCondition<G> {

    /**
     * Returns true when the run should terminate.
     */
    boolean shouldStop(AlgorithmState<G> state);

    /**
     * Condition identifier used in diagnostics.
     */
    String name();
}
