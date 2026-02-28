/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.impl;

import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.MixedRealDiscreteVector;

import java.util.Arrays;

/**
 * Mixed real/discrete representation.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MixedRealDiscreteVectorRepresentation implements Representation<MixedRealDiscreteVector> {

    private final int realDimensions;
    private final int[] cardinalities;
    private final double lower;
    private final double upper;

    /**
     * Creates a new MixedRealDiscreteVectorRepresentation instance.
     *
     * @param realDimensions the realDimensions argument
     * @param cardinalities the cardinalities argument
     * @param lower the lower argument
     * @param upper the upper argument
     */
    public MixedRealDiscreteVectorRepresentation(int realDimensions, int[] cardinalities, double lower, double upper) {
        if (realDimensions < 0) {
            throw new IllegalArgumentException("realDimensions must be >= 0");
        }
        if (upper < lower) {
            throw new IllegalArgumentException("upper must be >= lower");
        }
        this.realDimensions = realDimensions;
        this.cardinalities = Arrays.copyOf(cardinalities, cardinalities.length);
        this.lower = lower;
        this.upper = upper;
    }

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "mixed-real-discrete-vector";
    }

    /**
     * Samples a random value in representation domain.
     *
     * @param rng random stream
     * @return the random
     */
    @Override
    public MixedRealDiscreteVector random(RngStream rng) {
        double[] real = new double[realDimensions];
        double span = upper - lower;
        for (int i = 0; i < real.length; i++) {
            real[i] = lower + rng.nextDouble() * span;
        }

        int[] discrete = new int[cardinalities.length];
        for (int i = 0; i < discrete.length; i++) {
            discrete[i] = rng.nextInt(Math.max(1, cardinalities[i]));
        }

        return new MixedRealDiscreteVector(real, discrete);
    }

    /**
     * Returns whether value is valid in representation domain.
     *
     * @param genotype encoded genotype value
     * @return true if valid; otherwise false
     */
    @Override
    public boolean isValid(MixedRealDiscreteVector genotype) {
        if (genotype == null || genotype.realPart().length != realDimensions || genotype.discretePart().length != cardinalities.length) {
            return false;
        }
        for (double value : genotype.realPart()) {
            if (value < lower || value > upper || Double.isNaN(value)) {
                return false;
            }
        }
        for (int i = 0; i < genotype.discretePart().length; i++) {
            if (genotype.discretePart()[i] < 0 || genotype.discretePart()[i] >= Math.max(1, cardinalities[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Repairs value to representation domain constraints.
     *
     * @param genotype encoded genotype value
     * @return the repair
     */
    @Override
    public MixedRealDiscreteVector repair(MixedRealDiscreteVector genotype) {
        if (genotype == null) {
            return new MixedRealDiscreteVector(new double[realDimensions], new int[cardinalities.length]);
        }
        double[] repairedReal = Arrays.copyOf(genotype.realPart(), realDimensions);
        for (int i = 0; i < repairedReal.length; i++) {
            repairedReal[i] = Math.max(lower, Math.min(upper, repairedReal[i]));
        }

        int[] repairedDiscrete = Arrays.copyOf(genotype.discretePart(), cardinalities.length);
        for (int i = 0; i < repairedDiscrete.length; i++) {
            int card = Math.max(1, cardinalities[i]);
            if (repairedDiscrete[i] < 0 || repairedDiscrete[i] >= card) {
                repairedDiscrete[i] = Math.floorMod(repairedDiscrete[i], card);
            }
        }

        return new MixedRealDiscreteVector(repairedReal, repairedDiscrete);
    }

    /**
     * Returns compact value summary.
     *
     * @param genotype encoded genotype value
     * @return the summarize
     */
    @Override
    public String summarize(MixedRealDiscreteVector genotype) {
        return genotype.toString();
    }
}
