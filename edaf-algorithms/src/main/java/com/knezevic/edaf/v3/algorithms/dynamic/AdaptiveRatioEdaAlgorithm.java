package com.knezevic.edaf.v3.algorithms.dynamic;

import com.knezevic.edaf.v3.core.api.AbstractEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.AlgorithmContext;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Population;
import com.knezevic.edaf.v3.core.metrics.PopulationMetrics;

/**
 * Shared adaptive-ratio EDA driver used by dynamic and noisy aliases.
 */
public abstract class AdaptiveRatioEdaAlgorithm<G> extends AbstractEdaAlgorithm<G> {

    private final String id;
    private final double minRatio;
    private final double maxRatio;
    private double selectionRatio;

    protected AdaptiveRatioEdaAlgorithm(String id, double selectionRatio, double minRatio, double maxRatio) {
        this.id = id;
        this.minRatio = clamp(minRatio, 0.01, 1.0);
        this.maxRatio = clamp(maxRatio, this.minRatio, 1.0);
        this.selectionRatio = clamp(selectionRatio, this.minRatio, this.maxRatio);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    protected int selectionSize(AlgorithmContext<G> context, Population<G> population) {
        return Math.max(1, (int) Math.round(population.size() * selectionRatio));
    }

    @Override
    protected void afterIteration(AlgorithmContext<G> context, Population<G> previous, Population<G> next) {
        double signal = improvementSignal(previous, next, context.problem().objectiveSense());
        adaptRatio(signal);
    }

    protected abstract void adaptRatio(double normalizedImprovement);

    protected final double ratio() {
        return selectionRatio;
    }

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

    protected static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
