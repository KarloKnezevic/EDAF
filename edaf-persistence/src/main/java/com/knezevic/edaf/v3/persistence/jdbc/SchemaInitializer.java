package com.knezevic.edaf.v3.persistence.jdbc;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;

/**
 * Initializes persistence schema from SQL migration file.
 */
public final class SchemaInitializer {

    private static final List<String> MANAGED_TABLES_DROP_ORDER = List.of(
            "coco_aggregates",
            "coco_trials",
            "coco_optimizer_configs",
            "coco_reference_results",
            "coco_campaigns",
            "run_objectives",
            "iterations",
            "checkpoints",
            "events",
            "experiment_params",
            "runs",
            "experiments"
    );

    private SchemaInitializer() {
        // utility class
    }

    public static void initialize(DataSource dataSource) {
        String sql = loadSql("/db/migration/V1__init.sql");
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            enableSQLiteForeignKeysIfNeeded(connection, statement);

            if (isLegacySchema(connection)) {
                dropManagedTables(statement);
            }

            executeSqlFragments(statement, sql);
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed initializing JDBC schema", e);
        }
    }

    private static void executeSqlFragments(Statement statement, String sql) throws SQLException {
        for (String fragment : sql.split(";")) {
            String trimmed = fragment.trim();
            if (!trimmed.isEmpty()) {
                statement.execute(trimmed);
            }
        }
    }

    private static void dropManagedTables(Statement statement) throws SQLException {
        for (String table : MANAGED_TABLES_DROP_ORDER) {
            statement.execute("DROP TABLE IF EXISTS " + table);
        }
    }

    private static boolean isLegacySchema(Connection connection) throws SQLException {
        if (!tableExists(connection, "runs")) {
            return false;
        }
        return !columnExists(connection, "runs", "experiment_id");
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String[] variants = tableVariants(tableName);
        for (String variant : variants) {
            try (ResultSet rs = metaData.getTables(null, null, variant, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String[] tableVariants = tableVariants(tableName);
        String[] columnVariants = tableVariants(columnName);
        for (String tableVariant : tableVariants) {
            for (String columnVariant : columnVariants) {
                try (ResultSet rs = metaData.getColumns(null, null, tableVariant, columnVariant)) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String[] tableVariants(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        String upper = name.toUpperCase(Locale.ROOT);
        if (lower.equals(upper)) {
            return new String[]{name};
        }
        return new String[]{name, lower, upper};
    }

    private static void enableSQLiteForeignKeysIfNeeded(Connection connection, Statement statement) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        if (productName != null && productName.toLowerCase(Locale.ROOT).contains("sqlite")) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }

    private static String loadSql(String resource) {
        try (InputStream stream = SchemaInitializer.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Migration resource not found: " + resource);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed reading migration resource " + resource, e);
        }
    }
}
