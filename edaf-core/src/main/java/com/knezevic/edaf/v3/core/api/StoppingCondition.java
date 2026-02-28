/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

/**
 * Defines when an algorithm run should stop.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface StoppingCondition<G> {

    /**
     * Returns whether the current run state satisfies termination criteria.
     *
     * @param state current algorithm state snapshot
     * @return true when the run should terminate
     */
    boolean shouldStop(AlgorithmState<G> state);

    /**
     * Returns the condition identifier used in diagnostics.
     *
     * @return stopping condition identifier
     */
    String name();
}
