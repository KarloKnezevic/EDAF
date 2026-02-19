package com.knezevic.edaf.v3.persistence.jdbc;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies selective legacy reset behavior in schema initializer.
 */
class SchemaInitializerTest {

    @Test
    void legacySchemaIsDroppedAndRecreated() throws Exception {
        DataSource ds = sqliteDataSource("legacy-reset");

        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE runs (
                        run_id TEXT PRIMARY KEY,
                        algorithm TEXT NOT NULL,
                        model TEXT NOT NULL,
                        problem TEXT NOT NULL,
                        start_time TEXT NOT NULL,
                        seed BIGINT NOT NULL
                    )
                    """);
            statement.execute("INSERT INTO runs(run_id, algorithm, model, problem, start_time, seed) VALUES ('r1','a','m','p','2026-01-01T00:00:00Z',1)");
        }

        SchemaInitializer.initialize(ds);

        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            assertTrue(columnExists(connection, "runs", "experiment_id"));
            assertTrue(tableExists(connection, "experiments"));
            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM runs")) {
                rs.next();
                assertEquals(0, rs.getInt(1));
            }
        }
    }

    @Test
    void currentSchemaIsPreservedAcrossInitializeCalls() throws Exception {
        DataSource ds = sqliteDataSource("schema-preserve");
        SchemaInitializer.initialize(ds);

        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO experiments(
                        experiment_id, config_hash, schema_version, run_name, algorithm_type, model_type,
                        problem_type, representation_type, selection_type, replacement_type, stopping_type,
                        max_iterations, config_yaml, config_json, created_at
                    ) VALUES (
                        'exp1', 'hash1', '3.0', 'run name', 'umda', 'umda-bernoulli',
                        'onemax', 'bitstring', 'truncation', 'elitist', 'max-iterations',
                        100, 'yaml', '{"run":1}', '2026-01-01T00:00:00Z'
                    )
                    """);
            statement.execute("""
                    INSERT INTO runs(run_id, experiment_id, seed, status, start_time)
                    VALUES ('run-1', 'exp1', 7, 'COMPLETED', '2026-01-01T00:00:00Z')
                    """);
        }

        SchemaInitializer.initialize(ds);

        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM runs WHERE run_id = 'run-1'")) {
            rs.next();
            assertEquals(1, rs.getInt(1));
        }
    }

    private static DataSource sqliteDataSource(String suffix) throws Exception {
        Path db = Files.createTempFile("edaf-v3-" + suffix, ".db");
        return DataSourceFactory.create("jdbc:sqlite:" + db, "", "");
    }

    private static boolean tableExists(Connection connection, String tableName) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }
}
