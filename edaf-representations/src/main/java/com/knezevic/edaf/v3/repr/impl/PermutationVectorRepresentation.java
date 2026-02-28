/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.impl;

import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Arrays;

/**
 * Permutation representation built with in-place Fisher-Yates shuffle.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class PermutationVectorRepresentation implements Representation<PermutationVector> {

    private final int size;

    /**
     * Creates a new PermutationVectorRepresentation instance.
     *
     * @param size the size argument
     */
    public PermutationVectorRepresentation(int size) {
        if (size <= 1) {
            throw new IllegalArgumentException("size must be > 1");
        }
        this.size = size;
    }

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "permutation-vector";
    }

    /**
     * Samples a random value in representation domain.
     *
     * @param rng random stream
     * @return the random
     */
    @Override
    public PermutationVector random(RngStream rng) {
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = i;
        }
        for (int i = size - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
        return new PermutationVector(values);
    }

    /**
     * Returns whether value is valid in representation domain.
     *
     * @param genotype encoded genotype value
     * @return true if valid; otherwise false
     */
    @Override
    public boolean isValid(PermutationVector genotype) {
        if (genotype == null || genotype.size() != size) {
            return false;
        }
        boolean[] seen = new boolean[size];
        for (int value : genotype.order()) {
            if (value < 0 || value >= size || seen[value]) {
                return false;
            }
            seen[value] = true;
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
    public PermutationVector repair(PermutationVector genotype) {
        if (isValid(genotype)) {
            return genotype;
        }
        // Deterministic repair fallback to identity permutation when invalid.
        int[] identity = new int[size];
        for (int i = 0; i < size; i++) {
            identity[i] = i;
        }
        return new PermutationVector(identity);
    }

    /**
     * Returns compact value summary.
     *
     * @param genotype encoded genotype value
     * @return the summarize
     */
    @Override
    public String summarize(PermutationVector genotype) {
        return Arrays.toString(genotype.order());
    }
}
