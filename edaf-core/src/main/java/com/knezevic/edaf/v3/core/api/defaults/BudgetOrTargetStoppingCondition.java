package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.AlgorithmState;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.StoppingCondition;

/**
 * Stops when any configured budget/target criterion is reached:
 * max iterations, max evaluations, or target fitness.
 */
public final class BudgetOrTargetStoppingCondition<G> implements StoppingCondition<G> {

    private final int maxIterations;
    private final Long maxEvaluations;
    private final Double targetFitness;

    public BudgetOrTargetStoppingCondition(int maxIterations, Long maxEvaluations, Double targetFitness) {
        this.maxIterations = maxIterations;
        this.maxEvaluations = maxEvaluations;
        this.targetFitness = targetFitness;
    }

    @Override
    public boolean shouldStop(AlgorithmState<G> state) {
        if (state.iteration() >= maxIterations) {
            return true;
        }
        if (maxEvaluations != null && state.evaluations() >= maxEvaluations) {
            return true;
        }
        if (targetFitness != null) {
            double best = state.best().fitness().scalar();
            if (!Double.isFinite(best)) {
                return false;
            }
            ObjectiveSense sense = state.population().objectiveSense();
            return sense == ObjectiveSense.MINIMIZE
                    ? best <= targetFitness
                    : best >= targetFitness;
        }
        return false;
    }

    @Override
    public String name() {
        return "budget-or-target";
    }

    public int maxIterations() {
        return maxIterations;
    }

    public Long maxEvaluations() {
        return maxEvaluations;
    }

    public Double targetFitness() {
        return targetFitness;
    }
}
