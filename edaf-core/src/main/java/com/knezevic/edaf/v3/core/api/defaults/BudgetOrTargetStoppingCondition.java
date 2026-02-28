/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.AlgorithmState;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.StoppingCondition;

/**
 * Stops when any configured budget or target criterion is reached.
 * Supported criteria are max iterations, max evaluations, and target fitness.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BudgetOrTargetStoppingCondition<G> implements StoppingCondition<G> {

    private final int maxIterations;
    private final Long maxEvaluations;
    private final Double targetFitness;

    /**
     * Creates stopping condition with iteration, evaluation, and fitness targets.
     *
     * @param maxIterations maximum iteration count
     * @param maxEvaluations maximum evaluation count or null when disabled
     * @param targetFitness target fitness threshold or null when disabled
     */
    public BudgetOrTargetStoppingCondition(int maxIterations, Long maxEvaluations, Double targetFitness) {
        this.maxIterations = maxIterations;
        this.maxEvaluations = maxEvaluations;
        this.targetFitness = targetFitness;
    }

    /**
     * Returns whether current state satisfies any configured stop criterion.
     *
     * @param state current algorithm state
     * @return true when run should stop
     */
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

    /**
     * Returns stopping-condition identifier.
     *
     * @return condition identifier
     */
    @Override
    public String name() {
        return "budget-or-target";
    }

    /**
     * Returns maximum iteration count.
     *
     * @return max iteration count
     */
    public int maxIterations() {
        return maxIterations;
    }

    /**
     * Returns optional maximum evaluation count.
     *
     * @return max evaluation count or null
     */
    public Long maxEvaluations() {
        return maxEvaluations;
    }

    /**
     * Returns optional target fitness threshold.
     *
     * @return target fitness threshold or null
     */
    public Double targetFitness() {
        return targetFitness;
    }
}
