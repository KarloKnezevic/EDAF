package com.knezevic.edaf.persistence.api;

/**
 * Write port for persisting algorithm run data.
 * <p>
 * Implementations write run metadata, per-generation statistics,
 * and final results to a backing store (files, database, etc.).
 * Methods are called in lifecycle order:
 * {@link #onRunStarted} -> {@link #onGenerationCompleted}* -> {@link #onRunCompleted}.
 * </p>
 */
public interface ResultSink {

    /**
     * Called when an algorithm run begins.
     *
     * @param metadata run metadata including algorithm id, problem class, config hash, etc.
     */
    void onRunStarted(RunMetadata metadata);

    /**
     * Called after each generation completes.
     *
     * @param runId  the unique run identifier
     * @param record generation statistics snapshot
     */
    void onGenerationCompleted(String runId, GenerationRecord record);

    /**
     * Called when an algorithm run finishes (normally or by termination condition).
     *
     * @param runId  the unique run identifier
     * @param result run summary including total generations, best fitness, duration
     */
    void onRunCompleted(String runId, RunResult result);
}
