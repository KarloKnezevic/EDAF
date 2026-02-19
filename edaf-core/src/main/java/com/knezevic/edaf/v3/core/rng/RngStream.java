package com.knezevic.edaf.v3.core.rng;

import java.util.Objects;

/**
 * Named deterministic RNG stream used by one pipeline component.
 */
public final class RngStream {

    private final String name;
    private final StatefulRandom random;

    RngStream(String name, StatefulRandom random) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    public String name() {
        return name;
    }

    public double nextDouble() {
        return random.nextDouble();
    }

    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    public long nextLong() {
        return random.nextLong();
    }

    public double nextGaussian() {
        return random.nextGaussian();
    }

    RngStreamState snapshot() {
        return new RngStreamState(random.state(), random.hasGaussian(), random.gaussianCache());
    }

    void restore(RngStreamState state) {
        random.restore(state.state(), state.hasGaussian(), state.gaussian());
    }
}
