package com.knezevic.edaf.v3.core.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates active-run accounting and worker budgeting.
 */
class ExecutionParallelismTest {

    @Test
    void runLeaseTracksActiveRunsAndSuggestedWorkersStayPositive() {
        int initial = ExecutionParallelism.activeRuns();
        try (ExecutionParallelism.RunLease ignored = ExecutionParallelism.enterRun()) {
            assertEquals(initial + 1, ExecutionParallelism.activeRuns());
            int workers = ExecutionParallelism.suggestedFitnessWorkersPerRun();
            assertTrue(workers >= 1);
            assertTrue(workers <= ExecutionParallelism.availableProcessors());
        }
        assertEquals(initial, ExecutionParallelism.activeRuns());
    }
}
