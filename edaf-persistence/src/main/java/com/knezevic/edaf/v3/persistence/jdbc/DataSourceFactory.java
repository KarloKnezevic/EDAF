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
 * Creates pooled DataSource instances for JDBC sinks and queries.
 */
public final class DataSourceFactory {

    private DataSourceFactory() {
        // utility class
    }

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
            // SQLite uses file-level locks; a single pooled connection avoids self-contention.
            config.setMaximumPoolSize(1);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(30_000L);
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
