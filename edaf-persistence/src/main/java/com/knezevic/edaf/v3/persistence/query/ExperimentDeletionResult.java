/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

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
