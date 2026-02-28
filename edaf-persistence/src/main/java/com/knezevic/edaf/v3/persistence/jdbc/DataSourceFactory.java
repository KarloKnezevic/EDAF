/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Locale;

/**
 * Creates pooled {@link DataSource} instances used by EDAF persistence/query layers.
 *
 * <p>For SQLite, the factory applies concurrency-friendly defaults that reduce lock contention:
 * <ul>
 *     <li>{@code journal_mode=WAL}: enables concurrent readers with one writer.</li>
 *     <li>{@code busy_timeout=10000}: lets SQLite wait before returning {@code SQLITE_BUSY}.</li>
 *     <li>{@code synchronous=NORMAL}: practical durability/performance trade-off for run telemetry.</li>
 *     <li>small multi-connection pool: prevents local self-starvation between writer and stop-poll reads.</li>
 * </ul>
 *
 * <p>For non-SQLite JDBC URLs, conservative generic pool defaults are used.</p>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class DataSourceFactory {

    private static final int SQLITE_MAX_POOL_SIZE = 4;
    private static final long SQLITE_CONNECTION_TIMEOUT_MS = 15_000L;

    private DataSourceFactory() {
        // utility class
    }

    /**
     * Creates a configured JDBC {@link DataSource}.
     *
     * @param url JDBC URL
     * @param user database username
     * @param password database password
     * @return configured pooled datasource
     */
    public static DataSource create(String url, String user, String password) {
        String normalizedUrl = normalizeJdbcUrl(url);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(normalizedUrl);
        config.setUsername(user == null ? "" : user);
        config.setPassword(password == null ? "" : password);
        String driverClassName = resolveDriverClassName(normalizedUrl);
        if (driverClassName != null) {
            config.setDriverClassName(driverClassName);
        }
        // Do not fail-fast during pool construction; first real DB operation reports connectivity issues.
        config.setInitializationFailTimeout(-1L);
        if (isSqliteUrl(normalizedUrl)) {
            // WAL allows one writer + concurrent readers. A tiny pool avoids
            // stop-request polling starvation when writer sink is active.
            config.setMaximumPoolSize(SQLITE_MAX_POOL_SIZE);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(SQLITE_CONNECTION_TIMEOUT_MS);
        } else {
            config.setMaximumPoolSize(5);
        }
        config.setPoolName("edaf-v3-pool");
        return new HikariDataSource(config);
    }

    private static String normalizeJdbcUrl(String rawUrl) {
        String url = rawUrl == null || rawUrl.isBlank() ? "jdbc:sqlite:edaf-v3.db" : rawUrl.trim();
        if (!isSqliteUrl(url)) {
            return url;
        }

        // Ensure resilient defaults unless caller already set explicit values.
        String normalized = url;
        normalized = appendSqliteParamIfMissing(normalized, "busy_timeout", "10000");
        normalized = appendSqliteParamIfMissing(normalized, "journal_mode", "WAL");
        normalized = appendSqliteParamIfMissing(normalized, "synchronous", "NORMAL");
        normalized = appendSqliteParamIfMissing(normalized, "foreign_keys", "on");
        return normalized;
    }

    private static boolean isSqliteUrl(String url) {
        return url != null && url.toLowerCase(Locale.ROOT).startsWith("jdbc:sqlite:");
    }

    private static String resolveDriverClassName(String url) {
        if (url == null) {
            return null;
        }
        String lowered = url.toLowerCase(Locale.ROOT);
        if (lowered.startsWith("jdbc:sqlite:")) {
            return "org.sqlite.JDBC";
        }
        if (lowered.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        }
        return null;
    }

    private static String appendSqliteParamIfMissing(String url, String key, String value) {
        String lowered = url.toLowerCase(Locale.ROOT);
        String token = key.toLowerCase(Locale.ROOT) + "=";
        if (lowered.contains(token)) {
            return url;
        }
        return url + (url.contains("?") ? "&" : "?") + key + "=" + value;
    }
}
