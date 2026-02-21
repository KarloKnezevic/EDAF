package com.knezevic.edaf.v3.algorithms.dynamic;

/**
 * Memory-based dynamic EDA driver using EMA-smoothed improvement signal.
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
