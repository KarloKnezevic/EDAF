/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.dynamic;

/**
 * Memory-based dynamic EDA driver using EMA-smoothed improvement signal.
 *
 * <p>Selection ratio adaptation is driven by:
 * <pre>
 *   m_t = β m_{t-1} + (1-β) Δ_t
 * </pre>
 * where {@code Δ_t} is normalized best-fitness improvement and {@code β} is
 * {@code memoryDecay}. If {@code m_t} falls below target, the algorithm increases
 * exploration by raising elite ratio.
 *
 * <p>References:
 * <ol>
 *   <li>R. W. Eberhart and Y. Shi, "Tracking and optimizing dynamic systems with particle
 *   swarms," CEC, 2001. The same memory principle is adapted here to EDA ratio control.</li>
 *   <li>J. Branke, "Evolutionary Optimization in Dynamic Environments," Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MemoryEdaAlgorithm<G> extends AdaptiveRatioEdaAlgorithm<G> {

    private final double memoryDecay;
    private final double targetImprovement;
    private final double adjustmentStep;
    private double emaImprovement;
    private boolean initialized;

    public MemoryEdaAlgorithm(double selectionRatio,
                              double minRatio,
                              double maxRatio,
                              double memoryDecay,
                              double targetImprovement,
                              double adjustmentStep) {
        super("memory-eda", selectionRatio, minRatio, maxRatio);
        this.memoryDecay = clamp(memoryDecay, 0.0, 0.9999);
        this.targetImprovement = targetImprovement;
        this.adjustmentStep = Math.max(1.0e-4, adjustmentStep);
    }

    /**
     * Updates adaptive ratio from EMA-smoothed normalized improvement.
     *
     * @param normalizedImprovement normalized fitness improvement for current iteration
     */
    @Override
    protected void adaptRatio(double normalizedImprovement) {
        if (!initialized) {
            emaImprovement = normalizedImprovement;
            initialized = true;
        } else {
            emaImprovement = memoryDecay * emaImprovement + (1.0 - memoryDecay) * normalizedImprovement;
        }

        if (emaImprovement < targetImprovement) {
            setRatio(ratio() + adjustmentStep);
        } else {
            setRatio(ratio() - adjustmentStep);
        }
    }
}
