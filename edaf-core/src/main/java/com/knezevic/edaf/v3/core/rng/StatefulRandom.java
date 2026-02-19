package com.knezevic.edaf.v3.core.rng;

import java.io.Serial;
import java.io.Serializable;

/**
 * Lightweight deterministic RNG with explicit serializable state.
 *
 * This implementation uses xorshift64* for reproducible stream state persistence.
 */
public final class StatefulRandom implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long state;
    private boolean hasGaussian;
    private double gaussian;

    public StatefulRandom(long seed) {
        this.state = seed == 0L ? 0x9E3779B97F4A7C15L : seed;
    }

    public long state() {
        return state;
    }

    public void restore(long state, boolean hasGaussian, double gaussian) {
        this.state = state;
        this.hasGaussian = hasGaussian;
        this.gaussian = gaussian;
    }

    public boolean hasGaussian() {
        return hasGaussian;
    }

    public double gaussianCache() {
        return gaussian;
    }

    /**
     * Returns a uniformly distributed long.
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
     */
    public double nextDouble() {
        long bits = nextLong() >>> 11;
        return bits * 0x1.0p-53;
    }

    /**
     * Returns a uniformly distributed integer in [0, bound).
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
