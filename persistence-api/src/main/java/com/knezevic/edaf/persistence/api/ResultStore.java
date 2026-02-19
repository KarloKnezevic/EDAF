package com.knezevic.edaf.persistence.api;

import java.util.List;
import java.util.Optional;

/**
 * Read port for querying persisted algorithm run data.
 * <p>
 * Implementations retrieve run metadata, per-generation statistics,
 * and results from a backing store. Used by reporting and dashboard modules.
 * </p>
 */
public interface ResultStore {

    /**
     * Lists metadata for all stored runs, ordered by start time descending.
     *
     * @return list of run metadata records
     */
    List<RunMetadata> listRuns();

    /**
     * Retrieves metadata for a specific run.
     *
     * @param runId the unique run identifier
     * @return the run metadata, or empty if not found
     */
    Optional<RunMetadata> getRun(String runId);

    /**
     * Retrieves all generation records for a specific run, ordered by generation number.
     *
     * @param runId the unique run identifier
     * @return list of generation records
     */
    List<GenerationRecord> getGenerations(String runId);

    /**
     * Retrieves the final result for a specific run.
     *
     * @param runId the unique run identifier
     * @return the run result, or empty if the run has not completed
     */
    Optional<RunResult> getResult(String runId);
}
