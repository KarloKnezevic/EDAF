/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.jdbc;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies defensive stop-request behavior under transient datasource failures.
 */
class StopRequestStoreTest {

    @Test
    void stopCheckFailsOpenOnTransientConnectionTimeout() {
        StopRequestStore store = new StopRequestStore(failingDataSource(new SQLTransientConnectionException(
                "Connection is not available, request timed out"
        )));

        assertFalse(store.isStopRequested("run-1", "exp-1"));
    }

    @Test
    void stopAcknowledgementIsBestEffortOnTransientConnectionTimeout() {
        StopRequestStore store = new StopRequestStore(failingDataSource(new SQLTransientConnectionException(
                "Connection is not available, request timed out"
        )));

        assertEquals(0, store.acknowledgeStopRequests("run-1", "exp-1", null));
    }

    @Test
    void nonRetryableFailureStillPropagates() {
        StopRequestStore store = new StopRequestStore(failingDataSource(new SQLException("permission denied")));

        assertThrows(RuntimeException.class, () -> store.isStopRequested("run-1", "exp-1"));
    }

    private static DataSource failingDataSource(SQLException exception) {
        return new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                throw exception;
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                throw exception;
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                throw new SQLException("unwrap not supported");
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) {
                return false;
            }

            @Override
            public PrintWriter getLogWriter() {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter out) {
                // no-op
            }

            @Override
            public void setLoginTimeout(int seconds) {
                // no-op
            }

            @Override
            public int getLoginTimeout() {
                return 0;
            }

            @Override
            public Logger getParentLogger() {
                return Logger.getGlobal();
            }
        };
    }
}
