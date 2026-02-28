/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.dynamic;

import com.knezevic.edaf.v3.core.api.AbstractEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.AlgorithmContext;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Population;
import com.knezevic.edaf.v3.core.metrics.PopulationMetrics;

/**
 * Shared adaptive-ratio EDA driver used by dynamic and noisy aliases.
 *
 * <p>This base class augments the standard EDA cycle with an adaptive elite ratio:
 * <pre>
 *   r_{t+1} = clamp(r_t + Delta(r_t, s_t), r_min, r_max)
 * </pre>
 * where {@code s_t} is normalized inter-iteration improvement and
 * {@code Delta(...)} is implemented by subclasses (e.g. immigrants/noisy/sliding-window variants).
 *
 * <p>The default improvement signal is scale-normalized:
 * <pre>
 *   s_t = (best_t - best_{t+1}) / max(|best_t|, 1e-12)    [minimization]
 * </pre>
 * or sign-inverted for maximization.
 *
 * @param <G> genotype type
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public abstract class AdaptiveRatioEdaAlgorithm<G> extends AbstractEdaAlgorithm<G> {

    private final String id;
    private final double minRatio;
    private final double maxRatio;
    private double selectionRatio;

    /**
     * Creates a new AdaptiveRatioEdaAlgorithm instance.
     *
     * @param id algorithm identifier
     * @param selectionRatio fraction of population used for model fitting
     * @param minRatio lower bound for adaptive selection ratio
     * @param maxRatio upper bound for adaptive selection ratio
     */
    protected AdaptiveRatioEdaAlgorithm(String id, double selectionRatio, double minRatio, double maxRatio) {
        this.id = id;
        this.minRatio = clamp(minRatio, 0.01, 1.0);
        this.maxRatio = clamp(maxRatio, this.minRatio, 1.0);
        this.selectionRatio = clamp(selectionRatio, this.minRatio, this.maxRatio);
    }

    /**
     * Returns algorithm identifier.
     *
     * @return algorithm identifier
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Returns number of selected individuals used for model fitting.
     *
     * @param context algorithm runtime context
     * @param population current population
     * @return elite selection count
     */
    @Override
    protected int selectionSize(AlgorithmContext<G> context, Population<G> population) {
        return Math.max(1, (int) Math.round(population.size() * selectionRatio));
    }

    /**
     * Updates adaptive selection ratio after each completed iteration.
     *
     * @param context algorithm runtime context
     * @param previous previous population
     * @param next next population
     */
    @Override
    protected void afterIteration(AlgorithmContext<G> context, Population<G> previous, Population<G> next) {
        double signal = improvementSignal(previous, next, context.problem().objectiveSense());
        adaptRatio(signal);
    }

    /**
     * Applies subclass-specific ratio adaptation logic.
     *
     * @param normalizedImprovement scale-normalized improvement signal
     */
    protected abstract void adaptRatio(double normalizedImprovement);

    /**
     * Returns the current adaptive elite ratio.
     *
     * @return current selection ratio
     */
    protected final double ratio() {
        return selectionRatio;
    }

    /**
     * Sets adaptive ratio while enforcing configured bounds.
     *
     * @param value requested selection ratio
     */
    protected final void setRatio(double value) {
        selectionRatio = clamp(value, minRatio, maxRatio);
    }

    private static double improvementSignal(Population<?> previous, Population<?> next, ObjectiveSense sense) {
        double previousBest = PopulationMetrics.best(previous);
        double nextBest = PopulationMetrics.best(next);
        double scale = Math.max(1.0e-12, Math.abs(previousBest));
        if (sense == ObjectiveSense.MINIMIZE) {
            return (previousBest - nextBest) / scale;
        }
        return (nextBest - previousBest) / scale;
    }

    /**
     * Clamps value to the closed interval {@code [min, max]}.
     *
     * @param value value to clamp
     * @param min lower bound
     * @param max upper bound
     * @return clamped value
     */
    protected static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
