package com.knezevic.edaf.v3.persistence.query;

/**
 * Result of one experiment hard-delete operation.
 */
public record ExperimentDeletionResult(
        String experimentId,
        boolean deleted,
        int runsDeleted,
        int runObjectivesDeleted,
        int iterationsDeleted,
        int checkpointsDeleted,
        int eventsDeleted,
        int paramsDeleted
) {
}
