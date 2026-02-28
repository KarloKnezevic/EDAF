/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable snapshot of algorithm state used by stopping, restart, and metrics hooks.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class AlgorithmState<G> {

    private final String runId;
    private final String algorithmId;
    private final int iteration;
    private final long evaluations;
    private final Instant startedAt;
    private final Population<G> population;
    private final Individual<G> best;

    /**
     * Creates immutable algorithm state snapshot.
     *
     * @param runId run identifier
     * @param algorithmId algorithm identifier
     * @param iteration completed iteration index
     * @param evaluations total evaluation count
     * @param startedAt run start time
     * @param population current population
     * @param best current best individual
     */
    public AlgorithmState(String runId,
                          String algorithmId,
                          int iteration,
                          long evaluations,
                          Instant startedAt,
                          Population<G> population,
                          Individual<G> best) {
        this.runId = Objects.requireNonNull(runId, "runId must not be null");
        this.algorithmId = Objects.requireNonNull(algorithmId, "algorithmId must not be null");
        this.iteration = iteration;
        this.evaluations = evaluations;
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt must not be null");
        this.population = Objects.requireNonNull(population, "population must not be null");
        this.best = Objects.requireNonNull(best, "best must not be null");
    }

    /**
     * Returns run identifier.
     *
     * @return run identifier
     */
    public String runId() {
        return runId;
    }

    /**
     * Returns algorithm identifier.
     *
     * @return algorithm identifier
     */
    public String algorithmId() {
        return algorithmId;
    }

    /**
     * Returns current iteration index.
     *
     * @return iteration index
     */
    public int iteration() {
        return iteration;
    }

    /**
     * Returns total number of evaluations performed so far.
     *
     * @return evaluation count
     */
    public long evaluations() {
        return evaluations;
    }

    /**
     * Returns run start timestamp.
     *
     * @return start timestamp
     */
    public Instant startedAt() {
        return startedAt;
    }

    /**
     * Returns current population snapshot.
     *
     * @return current population
     */
    public Population<G> population() {
        return population;
    }

    /**
     * Returns best individual in current state.
     *
     * @return current best individual
     */
    public Individual<G> best() {
        return best;
    }

    /**
     * Returns elapsed wall-clock time between start and now.
     *
     * @return elapsed duration
     */
    public Duration elapsed() {
        return Duration.between(startedAt, Instant.now());
    }
}
