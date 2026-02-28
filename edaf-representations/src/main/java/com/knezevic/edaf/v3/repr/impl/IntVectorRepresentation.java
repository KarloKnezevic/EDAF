/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.impl;

import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.IntVector;

import java.util.Arrays;

/**
 * Bounded integer vector representation.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class IntVectorRepresentation implements Representation<IntVector> {

    private final int length;
    private final int min;
    private final int max;

    /**
     * Creates a new IntVectorRepresentation instance.
     *
     * @param length the length argument
     * @param min minimum value
     * @param max maximum value
     */
    public IntVectorRepresentation(int length, int min, int max) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be > 0");
        }
        if (max < min) {
            throw new IllegalArgumentException("max must be >= min");
        }
        this.length = length;
        this.min = min;
        this.max = max;
    }

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "int-vector";
    }

    /**
     * Samples a random value in representation domain.
     *
     * @param rng random stream
     * @return the random
     */
    @Override
    public IntVector random(RngStream rng) {
        int[] values = new int[length];
        int span = max - min + 1;
        for (int i = 0; i < length; i++) {
            values[i] = min + rng.nextInt(span);
        }
        return new IntVector(values);
    }

    /**
     * Returns whether value is valid in representation domain.
     *
     * @param genotype encoded genotype value
     * @return true if valid; otherwise false
     */
    @Override
    public boolean isValid(IntVector genotype) {
        if (genotype == null || genotype.length() != length) {
            return false;
        }
        for (int value : genotype.values()) {
            if (value < min || value > max) {
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
    public IntVector repair(IntVector genotype) {
        if (genotype == null) {
            return new IntVector(new int[length]);
        }
        int[] repaired = Arrays.copyOf(genotype.values(), length);
        for (int i = 0; i < repaired.length; i++) {
            repaired[i] = Math.max(min, Math.min(max, repaired[i]));
        }
        return new IntVector(repaired);
    }

    /**
     * Returns compact value summary.
     *
     * @param genotype encoded genotype value
     * @return the summarize
     */
    @Override
    public String summarize(IntVector genotype) {
        return genotype.toString();
    }
}
