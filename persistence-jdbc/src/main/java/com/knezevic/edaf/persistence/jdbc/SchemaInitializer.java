package com.knezevic.edaf.persistence.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the database schema using the bundled migration SQL.
 * <p>
 * Uses {@code CREATE TABLE IF NOT EXISTS} for idempotent execution.
 * </p>
 */
public final class SchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);
    private static final String SCHEMA_RESOURCE = "/db/migration/V1__init.sql";

    private SchemaInitializer() {}

    public static void initialize(DataSource dataSource) {
        String sql = loadSchema();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String ddl : sql.split(";")) {
                String trimmed = ddl.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
            log.info("Database schema initialized");
        } catch (SQLException e) {
            log.error("Failed to initialize database schema", e);
            throw new RuntimeException("Schema initialization failed", e);
        }
    }

    private static String loadSchema() {
        try (InputStream is = SchemaInitializer.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (is == null) {
                throw new RuntimeException("Schema resource not found: " + SCHEMA_RESOURCE);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read schema resource", e);
        }
    }
}
