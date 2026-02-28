/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.jdbc;

import javax.sql.DataSource;
import java.sql.SQLTransientConnectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Locale;

/**
 * Persists/queries cooperative stop requests for long-running runs and experiments.
 *
 * <p>This store is intentionally defensive because stop checks run inside hot optimization loops.
 * A transient persistence failure must never abort the optimization itself. Therefore:
 * <ul>
 *     <li>SQLite busy conditions are retried with bounded backoff.</li>
 *     <li>connection-pool exhaustion is treated as transient and handled fail-open for reads.</li>
 *     <li>acknowledgement writes are best effort during shutdown/finalization paths.</li>
 * </ul>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class StopRequestStore {

    private static final int SQLITE_BUSY_RETRIES = 6;
    private static final long SQLITE_BUSY_BACKOFF_MS = 40L;

    private final DataSource dataSource;

    /**
     * Creates stop-request store using configured datasource.
     *
     * @param dataSource JDBC datasource
     */
    public StopRequestStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Returns {@code true} when a pending stop request exists for run scope or experiment scope.
     *
     * @param runId run identifier
     * @param experimentId experiment identifier
     * @return true when stop is requested
     */
    public boolean isStopRequested(String runId, String experimentId) {
        if (!hasText(runId) && !hasText(experimentId)) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM control_requests
                WHERE action = 'STOP'
                  AND status = 'PENDING'
                  AND (
                        (scope = 'run' AND target_id = ?)
                        OR (scope = 'experiment' AND target_id = ?)
                  )
                LIMIT 1
                """;
        for (int attempt = 0; attempt <= SQLITE_BUSY_RETRIES; attempt++) {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, runId);
                statement.setString(2, experimentId);
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next();
                }
            } catch (Exception e) {
                if (isRetryable(e)) {
                    if (attempt == SQLITE_BUSY_RETRIES) {
                        // Fail-open: stop polling must not crash active optimization.
                        return false;
                    }
                    backoff(attempt);
                    continue;
                }
                throw new RuntimeException("Failed checking stop request state", e);
            }
        }
        return false;
    }

    /**
     * Acknowledges pending run/experiment stop requests consumed by this run.
     *
     * @param runId run identifier
     * @param experimentId experiment identifier
     * @param acknowledgedAt acknowledgement timestamp
     * @return number of rows acknowledged
     */
    public int acknowledgeStopRequests(String runId, String experimentId, Instant acknowledgedAt) {
        if (!hasText(runId) && !hasText(experimentId)) {
            return 0;
        }

        String sql = """
                UPDATE control_requests
                SET status = 'ACKNOWLEDGED',
                    acknowledged_at = ?,
                    acknowledged_by_run_id = ?
                WHERE action = 'STOP'
                  AND status = 'PENDING'
                  AND (
                        (scope = 'run' AND target_id = ?)
                        OR (scope = 'experiment' AND target_id = ?)
                  )
                """;
        for (int attempt = 0; attempt <= SQLITE_BUSY_RETRIES; attempt++) {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                String ts = acknowledgedAt == null ? Instant.now().toString() : acknowledgedAt.toString();
                statement.setString(1, ts);
                statement.setString(2, runId);
                statement.setString(3, runId);
                statement.setString(4, experimentId);
                return statement.executeUpdate();
            } catch (Exception e) {
                if (isRetryable(e)) {
                    if (attempt == SQLITE_BUSY_RETRIES) {
                        // Best-effort acknowledgement; do not fail run finalization on lock contention.
                        return 0;
                    }
                    backoff(attempt);
                    continue;
                }
                throw new RuntimeException("Failed acknowledging stop requests", e);
            }
        }
        return 0;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean isRetryable(Throwable throwable) {
        if (containsExceptionType(throwable, SQLTransientConnectionException.class)) {
            return true;
        }
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String lowered = message.toLowerCase(Locale.ROOT);
                if (lowered.contains("sqlite_busy")
                        || lowered.contains("database is locked")
                        || lowered.contains("connection is not available")
                        || lowered.contains("timeout")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean containsExceptionType(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static void backoff(int attempt) {
        long delayMs = SQLITE_BUSY_BACKOFF_MS * (attempt + 1L);
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
