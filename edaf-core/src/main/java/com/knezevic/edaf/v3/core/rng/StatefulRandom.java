/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.rng;

import java.io.Serial;
import java.io.Serializable;

/**
 * Lightweight deterministic RNG with explicit serializable state.
 *
 * This implementation uses xorshift64* for reproducible stream state persistence.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class StatefulRandom implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long state;
    private boolean hasGaussian;
    private double gaussian;

    /**
     * Creates a new StatefulRandom instance.
     *
     * @param seed seed value
     */
    public StatefulRandom(long seed) {
        this.state = seed == 0L ? 0x9E3779B97F4A7C15L : seed;
    }

    /**
     * Executes state.
     *
     * @return the computed state
     */
    public long state() {
        return state;
    }

    /**
     * Restores state from snapshot.
     *
     * @param state algorithm state
     * @param hasGaussian the hasGaussian argument
     * @param gaussian the gaussian argument
     */
    public void restore(long state, boolean hasGaussian, double gaussian) {
        this.state = state;
        this.hasGaussian = hasGaussian;
        this.gaussian = gaussian;
    }

    /**
     * Executes has gaussian.
     *
     * @return true if the instance has gaussian; otherwise false
     */
    public boolean hasGaussian() {
        return hasGaussian;
    }

    /**
     * Executes gaussian cache.
     *
     * @return the computed gaussian cache
     */
    public double gaussianCache() {
        return gaussian;
    }

    /**
     * Returns a uniformly distributed long.
     * @return the computed next long
     */
    public long nextLong() {
        long x = state;
        x ^= x >>> 12;
        x ^= x << 25;
        x ^= x >>> 27;
        state = x;
        return x * 2685821657736338717L;
    }

    /**
     * Returns a uniformly distributed double in [0, 1).
     * @return the computed next double
     */
    public double nextDouble() {
        long bits = nextLong() >>> 11;
        return bits * 0x1.0p-53;
    }

    /**
     * Returns a uniformly distributed integer in [0, bound).
     * @param bound the bound argument
     * @return the computed next int
     */
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be > 0");
        }
        long r = Long.remainderUnsigned(nextLong(), bound);
        return (int) r;
    }

    /**
     * Returns a normal distributed sample using Box-Muller transform.
     * @return the computed next gaussian
     */
    public double nextGaussian() {
        if (hasGaussian) {
            hasGaussian = false;
            return gaussian;
        }

        double u1;
        double u2;
        do {
            u1 = nextDouble();
            u2 = nextDouble();
        } while (u1 <= Double.MIN_NORMAL);

        double radius = Math.sqrt(-2.0 * Math.log(u1));
        double theta = 2.0 * Math.PI * u2;
        gaussian = radius * Math.sin(theta);
        hasGaussian = true;
        return radius * Math.cos(theta);
    }
}
