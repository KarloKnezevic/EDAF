package com.knezevic.edaf.v3.core.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable snapshot of algorithm state used by stopping, restart, and metrics hooks.
 */
public final class AlgorithmState<G> {

    private final String runId;
    private final String algorithmId;
    private final int iteration;
    private final long evaluations;
    private final Instant startedAt;
    private final Population<G> population;
    private final Individual<G> best;

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

    public String runId() {
        return runId;
    }

    public String algorithmId() {
        return algorithmId;
    }

    public int iteration() {
        return iteration;
    }

    public long evaluations() {
        return evaluations;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Population<G> population() {
        return population;
    }

    public Individual<G> best() {
        return best;
    }

    public Duration elapsed() {
        return Duration.between(startedAt, Instant.now());
    }
}
