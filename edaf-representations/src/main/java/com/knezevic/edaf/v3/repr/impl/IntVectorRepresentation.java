package com.knezevic.edaf.v3.repr.impl;

import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.IntVector;

import java.util.Arrays;

/**
 * Bounded integer vector representation.
 */
public final class IntVectorRepresentation implements Representation<IntVector> {

    private final int length;
    private final int min;
    private final int max;

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

    @Override
    public String type() {
        return "int-vector";
    }

    @Override
    public IntVector random(RngStream rng) {
        int[] values = new int[length];
        int span = max - min + 1;
        for (int i = 0; i < length; i++) {
            values[i] = min + rng.nextInt(span);
        }
        return new IntVector(values);
    }

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

    @Override
    public String summarize(IntVector genotype) {
        return genotype.toString();
    }
}
