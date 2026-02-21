package com.knezevic.edaf.v3.algorithms.dynamic;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Dynamic EDA driver that adapts selection pressure over a sliding improvement window.
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
