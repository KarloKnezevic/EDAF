/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * End-of-run summary used by CLI output, persistence, and reports.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class RunResult<G> {

    private final String runId;
    private final String algorithmId;
    private final String problemName;
    private final Individual<G> best;
    private final int iterations;
    private final long evaluations;
    private final Duration runtime;
    private final Map<String, String> artifacts;

    /**
     * Creates immutable run summary used by persistence, CLI, and reporting.
     *
     * @param runId run identifier
     * @param algorithmId algorithm identifier
     * @param problemName problem identifier
     * @param best best individual found in run
     * @param iterations completed iterations
     * @param evaluations total fitness evaluations
     * @param runtime run wall-clock duration
     * @param artifacts artifact map with output file locations
     */
    public RunResult(String runId,
                     String algorithmId,
                     String problemName,
                     Individual<G> best,
                     int iterations,
                     long evaluations,
                     Duration runtime,
                     Map<String, String> artifacts) {
        this.runId = Objects.requireNonNull(runId, "runId must not be null");
        this.algorithmId = Objects.requireNonNull(algorithmId, "algorithmId must not be null");
        this.problemName = Objects.requireNonNull(problemName, "problemName must not be null");
        this.best = Objects.requireNonNull(best, "best must not be null");
        this.iterations = iterations;
        this.evaluations = evaluations;
        this.runtime = Objects.requireNonNull(runtime, "runtime must not be null");
        this.artifacts = Collections.unmodifiableMap(new LinkedHashMap<>(artifacts));
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
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    public String problemName() {
        return problemName;
    }

    /**
     * Returns best individual found in the run.
     *
     * @return best individual
     */
    public Individual<G> best() {
        return best;
    }

    /**
     * Returns completed iteration count.
     *
     * @return number of completed iterations
     */
    public int iterations() {
        return iterations;
    }

    /**
     * Returns total number of fitness evaluations.
     *
     * @return evaluation count
     */
    public long evaluations() {
        return evaluations;
    }

    /**
     * Returns total runtime duration.
     *
     * @return runtime duration
     */
    public Duration runtime() {
        return runtime;
    }

    /**
     * Returns immutable artifact path map.
     *
     * @return artifact map
     */
    public Map<String, String> artifacts() {
        return artifacts;
    }
}
