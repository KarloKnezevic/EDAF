package com.knezevic.edaf.v3.repr.impl;

import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Arrays;

/**
 * Permutation representation built with in-place Fisher-Yates shuffle.
 */
public final class PermutationVectorRepresentation implements Representation<PermutationVector> {

    private final int size;

    public PermutationVectorRepresentation(int size) {
        if (size <= 1) {
            throw new IllegalArgumentException("size must be > 1");
        }
        this.size = size;
    }

    @Override
    public String type() {
        return "permutation-vector";
    }

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

    @Override
    public String summarize(PermutationVector genotype) {
        return Arrays.toString(genotype.order());
    }
}
