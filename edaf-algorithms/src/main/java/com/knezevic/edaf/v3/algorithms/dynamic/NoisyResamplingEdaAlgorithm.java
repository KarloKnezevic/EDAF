/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.dynamic;

import com.knezevic.edaf.v3.core.api.AlgorithmContext;
import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.core.api.VectorFitness;
import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Noisy-optimization EDA driver with per-candidate fitness resampling.
 *
 * <p>For each genotype, fitness is estimated by Monte Carlo averaging:
 * <pre>
 *   \hat{f}(x) = (1/R) Σ_{r=1}^{R} f_r(x)
 * </pre>
 * and noise level is tracked by EMA over sample variance. Selection ratio is then
 * adapted according to estimated noise-to-signal regime.
 *
 * <p>References:
 * <ol>
 *   <li>Y. Jin and J. Branke, "Evolutionary optimization in uncertain environments,"
 *   IEEE Transactions on Evolutionary Computation, 2005.</li>
 *   <li>H.-G. Beyer and B. Sendhoff, "Robust optimization: A comprehensive survey,"
 *   Computer Methods in Applied Mechanics and Engineering, 2007.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class NoisyResamplingEdaAlgorithm<G> extends AdaptiveRatioEdaAlgorithm<G> {

    private final int resamples;
    private final double noiseThreshold;
    private final double adjustmentStep;
    private double emaNoise;
    private boolean initialized;

    public NoisyResamplingEdaAlgorithm(double selectionRatio,
                                       double minRatio,
                                       double maxRatio,
                                       int resamples,
                                       double noiseThreshold,
                                       double adjustmentStep) {
        super("noisy-resampling-eda", selectionRatio, minRatio, maxRatio);
        this.resamples = Math.max(1, resamples);
        this.noiseThreshold = Math.max(0.0, noiseThreshold);
        this.adjustmentStep = Math.max(1.0e-4, adjustmentStep);
    }

    /**
     * Evaluates genotype using repeated noisy sampling and returns averaged fitness.
     *
     * @param context algorithm runtime context
     * @param feasibleGenotype candidate genotype after constraint handling
     * @param evaluationRng RNG stream dedicated to evaluation noise
     * @return averaged scalar or vector fitness estimate
     */
    @Override
    protected Fitness evaluateGenotype(AlgorithmContext<G> context, G feasibleGenotype, RngStream evaluationRng) {
        Fitness first = context.problem().evaluate(feasibleGenotype);
        if (resamples == 1) {
            return first;
        }

        double[] objectiveSums = first.objectives().clone();
        double scalarSum = first.scalar();
        double scalarSquaredSum = first.scalar() * first.scalar();

        for (int sample = 1; sample < resamples; sample++) {
            Fitness value = context.problem().evaluate(feasibleGenotype);
            scalarSum += value.scalar();
            scalarSquaredSum += value.scalar() * value.scalar();
            double[] objectives = value.objectives();
            for (int i = 0; i < objectiveSums.length; i++) {
                objectiveSums[i] += objectives[i];
            }
        }

        double inv = 1.0 / resamples;
        double scalarMean = scalarSum * inv;
        double variance = Math.max(0.0, scalarSquaredSum * inv - scalarMean * scalarMean);
        updateNoiseEstimate(variance);

        for (int i = 0; i < objectiveSums.length; i++) {
            objectiveSums[i] *= inv;
        }

        if (first.scalarNative()) {
            return new ScalarFitness(scalarMean);
        }
        return new VectorFitness(objectiveSums, scalarMean);
    }

    /**
     * Updates adaptive ratio using noise estimate and improvement signal.
     *
     * @param normalizedImprovement normalized fitness improvement for current iteration
     */
    @Override
    protected void adaptRatio(double normalizedImprovement) {
        if (emaNoise > noiseThreshold) {
            setRatio(ratio() + adjustmentStep);
        } else {
            // If noise is low, follow improvement signal.
            if (normalizedImprovement > 0.0) {
                setRatio(ratio() - 0.5 * adjustmentStep);
            } else {
                setRatio(ratio() + 0.25 * adjustmentStep);
            }
        }
    }

    private void updateNoiseEstimate(double variance) {
        if (!initialized) {
            emaNoise = variance;
            initialized = true;
            return;
        }
        emaNoise = 0.9 * emaNoise + 0.1 * variance;
    }
}
