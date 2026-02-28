/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.rng;

import java.util.Objects;

/**
 * Named deterministic RNG stream used by one pipeline component.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class RngStream {

    private final String name;
    private final StatefulRandom random;

    RngStream(String name, StatefulRandom random) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    /**
     * Returns component name identifier.
     *
     * @return component name
     */
    public String name() {
        return name;
    }

    /**
     * Executes next double.
     *
     * @return the computed next double
     */
    public double nextDouble() {
        return random.nextDouble();
    }

    /**
     * Executes next int.
     *
     * @param bound the bound argument
     * @return the computed next int
     */
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    /**
     * Executes next long.
     *
     * @return the computed next long
     */
    public long nextLong() {
        return random.nextLong();
    }

    /**
     * Executes next gaussian.
     *
     * @return the computed next gaussian
     */
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
