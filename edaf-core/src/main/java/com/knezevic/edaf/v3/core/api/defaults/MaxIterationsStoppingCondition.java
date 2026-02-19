package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.AlgorithmState;
import com.knezevic.edaf.v3.core.api.StoppingCondition;

/**
 * Stop condition based on maximum number of iterations.
 */
public final class MaxIterationsStoppingCondition<G> implements StoppingCondition<G> {

    private final int maxIterations;

    public MaxIterationsStoppingCondition(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    @Override
    public boolean shouldStop(AlgorithmState<G> state) {
        return state.iteration() >= maxIterations;
    }

    @Override
    public String name() {
        return "max-iterations";
    }

    public int maxIterations() {
        return maxIterations;
    }
}
