package com.knezevic.edaf.persistence.jdbc;

import com.knezevic.edaf.persistence.api.GenerationRecord;
import com.knezevic.edaf.persistence.api.ResultStore;
import com.knezevic.edaf.persistence.api.RunMetadata;
import com.knezevic.edaf.persistence.api.RunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link ResultStore} for reading persisted run data.
 */
public class JdbcResultStore implements ResultStore {

    private static final Logger log = LoggerFactory.getLogger(JdbcResultStore.class);
    private final DataSource dataSource;

    public JdbcResultStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<RunMetadata> listRuns() {
        List<RunMetadata> runs = new ArrayList<>();
        String sql = "SELECT run_id, algorithm_id, problem_class, genotype_type, " +
            "population_size, max_generations, config_hash, seed, started_at " +
            "FROM runs ORDER BY started_at DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                runs.add(mapRunMetadata(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to list runs", e);
        }
        return runs;
    }

    @Override
    public Optional<RunMetadata> getRun(String runId) {
        String sql = "SELECT run_id, algorithm_id, problem_class, genotype_type, " +
            "population_size, max_generations, config_hash, seed, started_at " +
            "FROM runs WHERE run_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRunMetadata(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to get run {}", runId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<GenerationRecord> getGenerations(String runId) {
        List<GenerationRecord> records = new ArrayList<>();
        String sql = "SELECT generation, best_fitness, worst_fitness, avg_fitness, " +
            "std_fitness, best_individual, eval_duration_nanos, recorded_at " +
            "FROM generation_stats WHERE run_id = ? ORDER BY generation";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(new GenerationRecord(
                        rs.getInt("generation"),
                        rs.getDouble("best_fitness"),
                        rs.getDouble("worst_fitness"),
                        rs.getDouble("avg_fitness"),
                        rs.getDouble("std_fitness"),
                        rs.getString("best_individual"),
                        rs.getLong("eval_duration_nanos"),
                        rs.getTimestamp("recorded_at").toInstant()
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to get generations for run {}", runId, e);
        }
        return records;
    }

    @Override
    public Optional<RunResult> getResult(String runId) {
        String sql = "SELECT total_generations, best_fitness, best_individual, " +
            "total_duration_ms, completed_at FROM runs WHERE run_id = ? AND status = 'COMPLETED'";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new RunResult(
                        rs.getInt("total_generations"),
                        rs.getDouble("best_fitness"),
                        rs.getString("best_individual"),
                        rs.getLong("total_duration_ms"),
                        rs.getTimestamp("completed_at").toInstant()
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to get result for run {}", runId, e);
        }
        return Optional.empty();
    }

    private RunMetadata mapRunMetadata(ResultSet rs) throws SQLException {
        Long seed = rs.getLong("seed");
        if (rs.wasNull()) seed = null;

        return new RunMetadata(
            rs.getString("run_id"),
            rs.getString("algorithm_id"),
            rs.getString("problem_class"),
            rs.getString("genotype_type"),
            rs.getInt("population_size"),
            rs.getInt("max_generations"),
            rs.getString("config_hash"),
            seed,
            rs.getTimestamp("started_at").toInstant()
        );
    }
}
