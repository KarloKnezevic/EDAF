/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.web.service;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Computes lightweight global dashboard statistics used by header summary cards.
 */
@Service
public final class DashboardStatsService {

    private final DataSource dataSource;

    public DashboardStatsService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Reads global database-backed summary counters for header presentation.
     */
    public DashboardSummary readSummary() {
        String countersSql = """
                SELECT
                    (SELECT COUNT(*) FROM experiments) AS experiment_count,
                    (SELECT COUNT(*) FROM runs) AS run_count,
                    (SELECT COALESCE(SUM(runtime_millis), 0) FROM runs) AS total_runtime_millis
                """;
        try (Connection connection = dataSource.getConnection()) {
            long experiments = 0L;
            long runs = 0L;
            long totalRuntimeMillis = 0L;
            try (PreparedStatement statement = connection.prepareStatement(countersSql);
                 ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    experiments = rs.getLong("experiment_count");
                    runs = rs.getLong("run_count");
                    totalRuntimeMillis = rs.getLong("total_runtime_millis");
                }
            }
            Long dbSizeBytes = resolveDatabaseSizeBytes(connection);
            return new DashboardSummary(
                    experiments,
                    runs,
                    totalRuntimeMillis,
                    dbSizeBytes,
                    dbSizeBytes == null ? null : bytesToGigabytes(dbSizeBytes),
                    formatGb(dbSizeBytes == null ? null : bytesToGigabytes(dbSizeBytes)),
                    formatCount(experiments),
                    formatCount(runs),
                    formatCount(totalRuntimeMillis)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed reading dashboard summary.", e);
        }
    }

    private static String formatCount(long value) {
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.US);
        return format.format(value);
    }

    private static String formatGb(Double value) {
        if (value == null || value.isNaN()) {
            return "-";
        }
        DecimalFormat format = new DecimalFormat("0.000", DecimalFormatSymbols.getInstance(Locale.US));
        return format.format(value) + " GB";
    }

    private static double bytesToGigabytes(long bytes) {
        return bytes / 1_073_741_824.0d;
    }

    private static Long resolveDatabaseSizeBytes(Connection connection) {
        try {
            String jdbcUrl = connection.getMetaData().getURL();
            if (jdbcUrl == null) {
                return null;
            }
            String lowered = jdbcUrl.toLowerCase(Locale.ROOT);
            if (lowered.startsWith("jdbc:sqlite:")) {
                return sqliteFileSizeBytes(jdbcUrl);
            }
            if (lowered.startsWith("jdbc:postgresql:")) {
                return postgresDatabaseSizeBytes(connection);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Long sqliteFileSizeBytes(String jdbcUrl) {
        String rawPath = jdbcUrl.substring("jdbc:sqlite:".length());
        int queryIdx = rawPath.indexOf('?');
        if (queryIdx >= 0) {
            rawPath = rawPath.substring(0, queryIdx);
        }
        if (rawPath.isBlank() || ":memory:".equals(rawPath)) {
            return null;
        }
        try {
            Path dbPath;
            if (rawPath.startsWith("file:")) {
                dbPath = Paths.get(URI.create(rawPath));
            } else {
                dbPath = Paths.get(rawPath);
            }
            if (!dbPath.isAbsolute()) {
                dbPath = Paths.get(System.getProperty("user.dir")).resolve(dbPath).normalize();
            }
            if (!Files.exists(dbPath)) {
                return null;
            }
            return Files.size(dbPath);
        } catch (Exception e) {
            return null;
        }
    }

    private static Long postgresDatabaseSizeBytes(Connection connection) {
        String sql = "SELECT pg_database_size(current_database())";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Header summary payload DTO.
     */
    public record DashboardSummary(
            long experimentCount,
            long runCount,
            long totalRuntimeMillis,
            Long databaseSizeBytes,
            Double databaseSizeGb,
            String databaseSizeDisplay,
            String experimentCountDisplay,
            String runCountDisplay,
            String totalRuntimeMillisDisplay
    ) {
    }
}
