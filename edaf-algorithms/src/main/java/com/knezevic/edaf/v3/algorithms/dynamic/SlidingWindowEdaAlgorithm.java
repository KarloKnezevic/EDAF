/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.dynamic;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Dynamic EDA driver that adapts selection pressure over a sliding improvement window.
 *
 * <p>The algorithm keeps last {@code windowSize} normalized improvements and adjusts ratio
 * according to window average:
 * <pre>
 *   \bar{Δ}_t = (1/W) Σ_{k=t-W+1}^{t} Δ_k
 * </pre>
 * If {@code \bar{Δ}_t < targetImprovement}, selection pressure is reduced by increasing
 * elite ratio (exploration boost).
 *
 * <p>References:
 * <ol>
 *   <li>J. Branke, "Evolutionary Optimization in Dynamic Environments," Kluwer, 2001.</li>
 *   <li>S. Yang and X. Yao, "Population-Based Incremental Learning in Dynamic Environments,"
 *   IEEE Transactions on Evolutionary Computation, 2008.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class SlidingWindowEdaAlgorithm<G> extends AdaptiveRatioEdaAlgorithm<G> {

    private final int windowSize;
    private final double targetImprovement;
    private final double adjustmentStep;
    private final Deque<Double> window;

    public SlidingWindowEdaAlgorithm(double selectionRatio,
                                     double minRatio,
                                     double maxRatio,
                                     int windowSize,
                                     double targetImprovement,
                                     double adjustmentStep) {
        super("sliding-window-eda", selectionRatio, minRatio, maxRatio);
        this.windowSize = Math.max(1, windowSize);
        this.targetImprovement = targetImprovement;
        this.adjustmentStep = Math.max(1.0e-4, adjustmentStep);
        this.window = new ArrayDeque<>(this.windowSize);
    }

    /**
     * Updates adaptive ratio from sliding-window average improvement.
     *
     * @param normalizedImprovement normalized fitness improvement for current iteration
     */
    @Override
    protected void adaptRatio(double normalizedImprovement) {
        window.addLast(normalizedImprovement);
        if (window.size() > windowSize) {
            window.removeFirst();
        }

        double sum = 0.0;
        for (double value : window) {
            sum += value;
        }
        double average = sum / window.size();
        if (average < targetImprovement) {
            setRatio(ratio() + adjustmentStep);
        } else {
            setRatio(ratio() - 0.5 * adjustmentStep);
        }
    }
}
