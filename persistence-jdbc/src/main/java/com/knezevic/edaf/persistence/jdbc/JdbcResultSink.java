package com.knezevic.edaf.persistence.jdbc;

import com.knezevic.edaf.persistence.api.GenerationRecord;
import com.knezevic.edaf.persistence.api.ResultSink;
import com.knezevic.edaf.persistence.api.RunMetadata;
import com.knezevic.edaf.persistence.api.RunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link ResultSink}.
 * <p>
 * Inserts run metadata, buffers generation records, and flushes
 * in batches of {@value #BATCH_SIZE} for performance.
 * </p>
 */
public class JdbcResultSink implements ResultSink {

    private static final Logger log = LoggerFactory.getLogger(JdbcResultSink.class);
    private static final int BATCH_SIZE = 50;

    private static final String INSERT_RUN =
        "INSERT INTO runs (run_id, algorithm_id, problem_class, genotype_type, " +
        "population_size, max_generations, config_hash, seed, started_at, status) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'RUNNING')";

    private static final String INSERT_GEN =
        "INSERT INTO generation_stats (run_id, generation, best_fitness, worst_fitness, " +
        "avg_fitness, std_fitness, best_individual, eval_duration_nanos, recorded_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_RUN =
        "UPDATE runs SET completed_at = ?, total_generations = ?, best_fitness = ?, " +
        "best_individual = ?, total_duration_ms = ?, status = 'COMPLETED' WHERE run_id = ?";

    private final DataSource dataSource;
    private final List<GenerationRecord> buffer = new ArrayList<>();
    private String currentRunId;

    public JdbcResultSink(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void onRunStarted(RunMetadata metadata) {
        this.currentRunId = metadata.runId();
        this.buffer.clear();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_RUN)) {
            ps.setString(1, metadata.runId());
            ps.setString(2, metadata.algorithmId());
            ps.setString(3, metadata.problemClass());
            ps.setString(4, metadata.genotypeType());
            ps.setInt(5, metadata.populationSize());
            ps.setInt(6, metadata.maxGenerations());
            if (metadata.configHash() != null) {
                ps.setString(7, metadata.configHash());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
            if (metadata.seed() != null) {
                ps.setLong(8, metadata.seed());
            } else {
                ps.setNull(8, Types.BIGINT);
            }
            ps.setTimestamp(9, Timestamp.from(metadata.startedAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to insert run metadata for {}", metadata.runId(), e);
        }
    }

    @Override
    public void onGenerationCompleted(String runId, GenerationRecord record) {
        buffer.add(record);
        if (buffer.size() >= BATCH_SIZE) {
            flushBuffer(runId);
        }
    }

    @Override
    public void onRunCompleted(String runId, RunResult result) {
        // Flush remaining buffered generations
        if (!buffer.isEmpty()) {
            flushBuffer(runId);
        }

        // Update run with final results
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_RUN)) {
            ps.setTimestamp(1, Timestamp.from(result.completedAt()));
            ps.setInt(2, result.totalGenerations());
            ps.setDouble(3, result.bestFitness());
            ps.setString(4, result.bestIndividualJson());
            ps.setLong(5, result.totalDurationMillis());
            ps.setString(6, runId);
            ps.executeUpdate();
            log.info("Run {} completed and persisted to database", runId);
        } catch (SQLException e) {
            log.error("Failed to update run result for {}", runId, e);
        }
    }

    private void flushBuffer(String runId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_GEN)) {
            for (GenerationRecord record : buffer) {
                ps.setString(1, runId);
                ps.setInt(2, record.generation());
                ps.setDouble(3, record.bestFitness());
                ps.setDouble(4, record.worstFitness());
                ps.setDouble(5, record.avgFitness());
                ps.setDouble(6, record.stdFitness());
                ps.setString(7, record.bestIndividualJson());
                ps.setLong(8, record.evalDurationNanos());
                ps.setTimestamp(9, Timestamp.from(record.recordedAt()));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            log.error("Failed to flush generation batch for run {}", runId, e);
        }
        buffer.clear();
    }
}
