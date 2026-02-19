package com.knezevic.edaf.persistence.jdbc;

import com.knezevic.edaf.persistence.api.GenerationRecord;
import com.knezevic.edaf.persistence.api.RunMetadata;
import com.knezevic.edaf.persistence.api.RunResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JdbcResultSinkTest {

    @TempDir
    Path tempDir;

    private DataSource dataSource;
    private JdbcResultSink sink;

    @BeforeEach
    void setUp() {
        String dbPath = tempDir.resolve("test.db").toString();
        dataSource = DataSourceFactory.create("jdbc:sqlite:" + dbPath, null, null);
        SchemaInitializer.initialize(dataSource);
        sink = new JdbcResultSink(dataSource);
    }

    @Test
    void persistsRunAndGenerations() throws Exception {
        String runId = "test-jdbc-run";
        RunMetadata metadata = new RunMetadata(
            runId, "nes", "com.test.Sphere", "fp",
            50, 200, null, 42L, Instant.now());

        sink.onRunStarted(metadata);

        // Insert generations
        for (int i = 1; i <= 10; i++) {
            sink.onGenerationCompleted(runId, new GenerationRecord(
                i, 100.0 / i, 200.0, 150.0, 10.0,
                "[" + i + ".0]", 500000L, Instant.now()));
        }

        sink.onRunCompleted(runId, new RunResult(
            10, 10.0, "[10.0]", 2000L, Instant.now()));

        // Verify run exists and is completed
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT status, total_generations, best_fitness FROM runs WHERE run_id = '" + runId + "'");
            assertTrue(rs.next());
            assertEquals("COMPLETED", rs.getString("status"));
            assertEquals(10, rs.getInt("total_generations"));
            assertEquals(10.0, rs.getDouble("best_fitness"), 0.001);
        }

        // Verify generation stats
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM generation_stats WHERE run_id = '" + runId + "'");
            assertTrue(rs.next());
            assertEquals(10, rs.getInt(1));
        }
    }

    @Test
    void resultStoreReadsPersistedData() {
        String runId = "store-test-run";
        RunMetadata metadata = new RunMetadata(
            runId, "cem", "com.test.Rastrigin", "binary",
            100, 500, "abc123", null, Instant.now());

        sink.onRunStarted(metadata);
        sink.onGenerationCompleted(runId, new GenerationRecord(
            1, 50.0, 100.0, 75.0, 12.0, null, 100000L, Instant.now()));
        sink.onRunCompleted(runId, new RunResult(
            1, 50.0, null, 100L, Instant.now()));

        // Read back via store
        JdbcResultStore store = new JdbcResultStore(dataSource);

        var runs = store.listRuns();
        assertFalse(runs.isEmpty());

        var run = store.getRun(runId);
        assertTrue(run.isPresent());
        assertEquals("cem", run.get().algorithmId());

        var generations = store.getGenerations(runId);
        assertEquals(1, generations.size());
        assertEquals(50.0, generations.get(0).bestFitness(), 0.001);

        var result = store.getResult(runId);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().totalGenerations());
    }
}
