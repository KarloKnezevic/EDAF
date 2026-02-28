/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * Result of one experiment hard-delete operation.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
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
