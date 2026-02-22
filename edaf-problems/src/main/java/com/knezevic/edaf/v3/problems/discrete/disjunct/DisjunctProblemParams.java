package com.knezevic.edaf.v3.problems.discrete.disjunct;

import com.knezevic.edaf.v3.core.util.Params;

import java.util.Map;
import java.util.Locale;

/**
 * Shared typed parameter parsing for DM/RM/ADM problem plugins.
 */
public record DisjunctProblemParams(
        int m,
        int n,
        int t,
        int f,
        double epsilon,
        DisjunctEvaluationConfig evaluation
) {

    /**
     * Parses common problem parameters from YAML plugin map.
     *
     * <p>Supported aliases:
     * {@code m|rows}, {@code n|columns}, {@code t}, {@code f}, {@code epsilon}.</p>
     */
    public static DisjunctProblemParams from(Map<String, Object> params) {
        int m = Params.integer(params, "m", Params.integer(params, "rows", -1));
        int n = Params.integer(params, "n", Params.integer(params, "columns", -1));
        int t = Params.integer(params, "t", -1);
        int f = Params.integer(params, "f", 0);
        double epsilon = Params.dbl(params, "epsilon", 0.0);

        String rawMode = Params.str(params, "evaluationMode", "auto").trim().toUpperCase(Locale.ROOT);
        DisjunctEvaluationMode mode = switch (rawMode) {
            case "EXACT" -> DisjunctEvaluationMode.EXACT;
            case "SAMPLED" -> DisjunctEvaluationMode.SAMPLED;
            case "AUTO" -> DisjunctEvaluationMode.AUTO;
            default -> throw new IllegalArgumentException(
                    "problem.evaluationMode must be one of exact|sampled|auto, got '" + rawMode + "'"
            );
        };
        long maxExactSubsets = Params.longValue(params, "maxExactSubsets", 1_000L);
        long sampleSize = Params.longValue(params, "sampleSize", 512L);
        long samplingSeed = Params.longValue(params, "samplingSeed", 7_331L);
        DisjunctEvaluationConfig evaluation = new DisjunctEvaluationConfig(
                mode,
                maxExactSubsets,
                sampleSize,
                samplingSeed
        );

        return new DisjunctProblemParams(m, n, t, f, epsilon, evaluation).validate();
    }

    /**
     * Expected genotype length for column-major encoding.
     */
    public int expectedGenomeLength() {
        return Math.multiplyExact(m, n);
    }

    private DisjunctProblemParams validate() {
        if (m <= 0) {
            throw new IllegalArgumentException("problem.m (or problem.rows) must be > 0");
        }
        if (n <= 1) {
            throw new IllegalArgumentException("problem.n (or problem.columns) must be > 1");
        }
        if (t < 1 || t >= n) {
            throw new IllegalArgumentException("problem.t must satisfy 1 <= t < N");
        }
        if (f < 0 || f >= n) {
            throw new IllegalArgumentException("problem.f must satisfy 0 <= f < N");
        }
        if (epsilon < 0.0 || epsilon > 1.0) {
            throw new IllegalArgumentException("problem.epsilon must be in [0,1]");
        }
        if (evaluation == null) {
            throw new IllegalArgumentException("evaluation config must not be null");
        }
        expectedGenomeLength();
        return this;
    }
}
