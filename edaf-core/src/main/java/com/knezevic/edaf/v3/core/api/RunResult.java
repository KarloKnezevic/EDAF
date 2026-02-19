package com.knezevic.edaf.v3.core.api;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * End-of-run summary used by CLI output, persistence, and reports.
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

    public String runId() {
        return runId;
    }

    public String algorithmId() {
        return algorithmId;
    }

    public String problemName() {
        return problemName;
    }

    public Individual<G> best() {
        return best;
    }

    public int iterations() {
        return iterations;
    }

    public long evaluations() {
        return evaluations;
    }

    public Duration runtime() {
        return runtime;
    }

    public Map<String, String> artifacts() {
        return artifacts;
    }
}
