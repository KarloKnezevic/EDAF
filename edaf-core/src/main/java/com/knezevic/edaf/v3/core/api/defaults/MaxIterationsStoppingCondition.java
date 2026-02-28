/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.AlgorithmState;
import com.knezevic.edaf.v3.core.api.StoppingCondition;

/**
 * Stop condition based on maximum number of iterations.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MaxIterationsStoppingCondition<G> implements StoppingCondition<G> {

    private final int maxIterations;

    /**
     * Creates stopping condition with fixed iteration budget.
     *
     * @param maxIterations maximum iteration count
     */
    public MaxIterationsStoppingCondition(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    /**
     * Returns whether current iteration index reached configured maximum.
     *
     * @param state current algorithm state
     * @return true when run should stop
     */
    @Override
    public boolean shouldStop(AlgorithmState<G> state) {
        return state.iteration() >= maxIterations;
    }

    /**
     * Returns stopping-condition identifier.
     *
     * @return condition identifier
     */
    @Override
    public String name() {
        return "max-iterations";
    }

    /**
     * Returns configured maximum iteration count.
     *
     * @return max iteration count
     */
    public int maxIterations() {
        return maxIterations;
    }
}
