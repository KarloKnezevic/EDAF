/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.runtime;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void suggestedWorkersDoNotIncreaseAsActiveRunsGrow() {
        int initial = ExecutionParallelism.activeRuns();
        List<ExecutionParallelism.RunLease> leases = new ArrayList<>();
        int previousWorkers = Integer.MAX_VALUE;
        try {
            int maxLeases = Math.max(2, ExecutionParallelism.availableProcessors() * 2);
            for (int i = 0; i < maxLeases; i++) {
                leases.add(ExecutionParallelism.enterRun());
                int currentWorkers = ExecutionParallelism.suggestedFitnessWorkersPerRun();
                assertTrue(currentWorkers >= 1);
                assertTrue(currentWorkers <= ExecutionParallelism.availableProcessors());
                assertTrue(currentWorkers <= previousWorkers,
                        "Worker budget should not increase as more runs become active");
                previousWorkers = currentWorkers;
            }
        } finally {
            closeAll(leases);
        }
        assertEquals(initial, ExecutionParallelism.activeRuns());
    }

    @Test
    void concurrentLeaseLifecycleRemainsBalanced() throws Exception {
        int initial = ExecutionParallelism.activeRuns();
        int threads = Math.max(2, Math.min(8, ExecutionParallelism.availableProcessors()));
        int iterationsPerThread = 500;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            futures.add(executor.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < iterationsPerThread; i++) {
                        try (ExecutionParallelism.RunLease ignored = ExecutionParallelism.enterRun()) {
                            assertTrue(ExecutionParallelism.activeRuns() >= 1);
                        }
                    }
                } catch (Throwable throwable) {
                    failure.compareAndSet(null, throwable);
                }
            }));
        }

        start.countDown();
        for (Future<?> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        assertNull(failure.get(), "Concurrent lease lifecycle should not throw");
        assertEquals(initial, ExecutionParallelism.activeRuns());
    }

    private static void closeAll(List<ExecutionParallelism.RunLease> leases) {
        for (int i = leases.size() - 1; i >= 0; i--) {
            leases.get(i).close();
        }
    }
}
