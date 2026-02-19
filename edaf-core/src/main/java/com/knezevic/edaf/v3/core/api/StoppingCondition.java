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
