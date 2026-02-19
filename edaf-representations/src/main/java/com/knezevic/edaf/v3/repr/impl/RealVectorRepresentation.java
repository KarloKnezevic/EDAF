package com.knezevic.edaf.v3.repr.impl;

import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Arrays;

/**
 * Real vector representation with optional per-dimension bounds.
 */
public final class RealVectorRepresentation implements Representation<RealVector> {

    private final int length;
    private final double lower;
    private final double upper;

    public RealVectorRepresentation(int length, double lower, double upper) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be > 0");
        }
        if (upper < lower) {
            throw new IllegalArgumentException("upper must be >= lower");
        }
        this.length = length;
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public String type() {
        return "real-vector";
    }

    @Override
    public RealVector random(RngStream rng) {
        double[] values = new double[length];
        double span = upper - lower;
        for (int i = 0; i < length; i++) {
            values[i] = lower + rng.nextDouble() * span;
        }
        return new RealVector(values);
    }

    @Override
    public boolean isValid(RealVector genotype) {
        if (genotype == null || genotype.length() != length) {
            return false;
        }
        for (double value : genotype.values()) {
            if (value < lower || value > upper || Double.isNaN(value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public RealVector repair(RealVector genotype) {
        if (genotype == null) {
            return new RealVector(new double[length]);
        }
        double[] repaired = Arrays.copyOf(genotype.values(), length);
        for (int i = 0; i < repaired.length; i++) {
            repaired[i] = Math.max(lower, Math.min(upper, repaired[i]));
        }
        return new RealVector(repaired);
    }

    @Override
    public String summarize(RealVector genotype) {
        return genotype.toString();
    }
}
