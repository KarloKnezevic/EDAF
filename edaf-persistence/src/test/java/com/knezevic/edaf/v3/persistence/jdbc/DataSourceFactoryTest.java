/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers URL normalization and driver selection for pooled data sources.
 */
class DataSourceFactoryTest {

    @Test
    void sqliteUrlEnablesSqliteTuningAndDriver() {
        DataSource dataSource = DataSourceFactory.create("jdbc:sqlite:/tmp/edaf-test.db", "", "");
        try (HikariDataSource hikari = (HikariDataSource) dataSource) {
            assertEquals("org.sqlite.JDBC", hikari.getDriverClassName());
            assertEquals(1, hikari.getMaximumPoolSize());
            String url = hikari.getJdbcUrl();
            assertTrue(url.contains("busy_timeout=10000"));
            assertTrue(url.contains("journal_mode=WAL"));
            assertTrue(url.contains("synchronous=NORMAL"));
            assertTrue(url.contains("foreign_keys=on"));
        }
    }

    @Test
    void postgresUrlUsesPostgresDriverAndNonSqlitePoolDefaults() {
        DataSource dataSource = DataSourceFactory.create("jdbc:postgresql://localhost:5432/edaf", "u", "p");
        try (HikariDataSource hikari = (HikariDataSource) dataSource) {
            assertEquals("org.postgresql.Driver", hikari.getDriverClassName());
            assertEquals(5, hikari.getMaximumPoolSize());
            assertEquals("jdbc:postgresql://localhost:5432/edaf", hikari.getJdbcUrl());
        }
    }
}

