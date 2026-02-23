package com.knezevic.edaf.v3.persistence.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;

/**
 * Persists cooperative stop requests and acknowledgements for long-running runs.
 */
public final class StopRequestStore {

    private final DataSource dataSource;

    public StopRequestStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Returns {@code true} when a pending stop request exists for run scope or experiment scope.
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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, runId);
            statement.setString(2, experimentId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed checking stop request state", e);
        }
    }

    /**
     * Acknowledges pending run/experiment stop requests consumed by this run.
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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String ts = acknowledgedAt == null ? Instant.now().toString() : acknowledgedAt.toString();
            statement.setString(1, ts);
            statement.setString(2, runId);
            statement.setString(3, runId);
            statement.setString(4, experimentId);
            return statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed acknowledging stop requests", e);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
