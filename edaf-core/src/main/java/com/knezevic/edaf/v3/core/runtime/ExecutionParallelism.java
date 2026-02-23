package com.knezevic.edaf.v3.core.runtime;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central runtime parallelism coordinator shared across run execution paths.
 *
 * <p>The framework executes two nested levels of work:
 * <ul>
 *     <li>run-level parallelism (multiple runs from batch/campaign)</li>
 *     <li>in-run fitness parallelism (candidate evaluation within one run)</li>
 * </ul>
 * This coordinator keeps global run activity count so in-run evaluators can
 * dynamically scale their worker budget and avoid CPU oversubscription.</p>
 */
public final class ExecutionParallelism {

    private static final int AVAILABLE_PROCESSORS = Math.max(1, Runtime.getRuntime().availableProcessors());
    private static final int BATCH_PARALLELISM_HINT = envInt("EDAF_BATCH_PARALLELISM",
            Math.max(1, AVAILABLE_PROCESSORS / 2));
    private static final int FITNESS_WORKER_CAP = envInt("EDAF_MAX_FITNESS_WORKERS", AVAILABLE_PROCESSORS);
    private static final AtomicInteger ACTIVE_RUNS = new AtomicInteger(0);

    private ExecutionParallelism() {
        // utility class
    }

    /**
     * Number of logical processors visible to the JVM.
     */
    public static int availableProcessors() {
        return AVAILABLE_PROCESSORS;
    }

    /**
     * Recommended run-level concurrency for batch/campaign orchestration.
     */
    public static int suggestedRunParallelism() {
        return Math.max(1, Math.min(BATCH_PARALLELISM_HINT, AVAILABLE_PROCESSORS));
    }

    /**
     * Number of runs currently executing in this JVM process.
     */
    public static int activeRuns() {
        return Math.max(0, ACTIVE_RUNS.get());
    }

    /**
     * Dynamic worker budget for one run fitness evaluator.
     *
     * <p>When multiple runs are active, each run gets a proportional share of available CPUs.</p>
     */
    public static int suggestedFitnessWorkersPerRun() {
        int active = Math.max(1, activeRuns());
        int share = Math.max(1, AVAILABLE_PROCESSORS / active);
        return Math.max(1, Math.min(FITNESS_WORKER_CAP, share));
    }

    /**
     * Marks one run as active and returns lease that must be closed when run finishes.
     */
    public static RunLease enterRun() {
        ACTIVE_RUNS.incrementAndGet();
        return new RunLease();
    }

    private static int envInt(String key, int defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Math.max(1, Integer.parseInt(value.trim()));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * AutoCloseable lease for active-run accounting.
     */
    public static final class RunLease implements AutoCloseable {

        private final AtomicBoolean closed = new AtomicBoolean(false);

        private RunLease() {
        }

        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            int remaining = ACTIVE_RUNS.decrementAndGet();
            if (remaining < 0) {
                ACTIVE_RUNS.set(0);
                throw new IllegalStateException(String.format(
                        Locale.ROOT,
                        "ExecutionParallelism active run counter became negative (value=%d)",
                        remaining
                ));
            }
        }
    }
}
