package com.knezevic.edaf.v3.persistence.query;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * JDBC read repository used by reporting and web dashboard.
 */
public final class JdbcRunRepository implements RunRepository {

    private static final Map<String, String> SORT_COLUMNS = new LinkedHashMap<>();

    static {
        SORT_COLUMNS.put("start_time", "r.start_time");
        SORT_COLUMNS.put("best_fitness", "r.best_fitness");
        SORT_COLUMNS.put("runtime_millis", "r.runtime_millis");
        SORT_COLUMNS.put("status", "r.status");
    }

    private final DataSource dataSource;

    public JdbcRunRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public PageResult<RunListItem> listRuns(RunQuery query) {
        RunQuery effective = normalize(query);
        WhereClause filters = buildRunFilters(effective);
        String sortColumn = SORT_COLUMNS.getOrDefault(effective.sortBy(), SORT_COLUMNS.get("start_time"));
        String sortDir = "asc".equalsIgnoreCase(effective.sortDir()) ? "ASC" : "DESC";

        String baseFrom = """
                FROM runs r
                JOIN experiments e ON e.experiment_id = r.experiment_id
                WHERE 1 = 1
                """;
        String whereSql = baseFrom + filters.sql();

        String countSql = "SELECT COUNT(*) " + whereSql;
        String dataSql = """
                SELECT
                    r.run_id,
                    r.experiment_id,
                    e.run_name,
                    e.algorithm_type,
                    e.model_type,
                    e.problem_type,
                    e.representation_type,
                    r.status,
                    r.start_time,
                    r.end_time,
                    r.iterations,
                    r.evaluations,
                    r.best_fitness,
                    r.runtime_millis,
                    e.config_hash
                """
                + whereSql
                + " ORDER BY " + sortColumn + " " + sortDir
                + " LIMIT ? OFFSET ?";

        try (Connection connection = dataSource.getConnection()) {
            long total = queryCount(connection, countSql, filters.params());

            List<Object> dataParams = new ArrayList<>(filters.params());
            dataParams.add(effective.size());
            dataParams.add(effective.offset());

            List<RunListItem> rows = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(dataSql)) {
                bindParams(statement, dataParams);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new RunListItem(
                                rs.getString("run_id"),
                                rs.getString("experiment_id"),
                                rs.getString("run_name"),
                                rs.getString("algorithm_type"),
                                rs.getString("model_type"),
                                rs.getString("problem_type"),
                                rs.getString("representation_type"),
                                rs.getString("status"),
                                rs.getString("start_time"),
                                rs.getString("end_time"),
                                getNullableInteger(rs, "iterations"),
                                getNullableLong(rs, "evaluations"),
                                getNullableDouble(rs, "best_fitness"),
                                getNullableLong(rs, "runtime_millis"),
                                rs.getString("config_hash")
                        ));
                    }
                }
            }

            long totalPages = total == 0 ? 0 : ((total + effective.size() - 1) / effective.size());
            return new PageResult<>(rows, effective.page(), effective.size(), total, totalPages);
        } catch (Exception e) {
            throw new RuntimeException("Failed listing runs", e);
        }
    }

    @Override
    public RunDetail getRunDetail(String runId) {
        String sql = """
                SELECT
                    r.run_id,
                    r.experiment_id,
                    e.config_hash,
                    e.schema_version,
                    e.run_name,
                    e.algorithm_type,
                    e.model_type,
                    e.problem_type,
                    e.representation_type,
                    e.selection_type,
                    e.replacement_type,
                    e.stopping_type,
                    e.max_iterations,
                    r.status,
                    r.seed,
                    r.start_time,
                    r.end_time,
                    r.iterations,
                    r.evaluations,
                    r.best_fitness,
                    r.best_summary,
                    r.runtime_millis,
                    r.artifacts_json,
                    r.resumed_from,
                    r.error_message,
                    e.config_yaml,
                    e.config_json,
                    e.created_at
                FROM runs r
                JOIN experiments e ON e.experiment_id = r.experiment_id
                WHERE r.run_id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, runId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new RunDetail(
                        rs.getString("run_id"),
                        rs.getString("experiment_id"),
                        rs.getString("config_hash"),
                        rs.getString("schema_version"),
                        rs.getString("run_name"),
                        rs.getString("algorithm_type"),
                        rs.getString("model_type"),
                        rs.getString("problem_type"),
                        rs.getString("representation_type"),
                        rs.getString("selection_type"),
                        rs.getString("replacement_type"),
                        rs.getString("stopping_type"),
                        getNullableInteger(rs, "max_iterations"),
                        rs.getString("status"),
                        rs.getLong("seed"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        getNullableInteger(rs, "iterations"),
                        getNullableLong(rs, "evaluations"),
                        getNullableDouble(rs, "best_fitness"),
                        rs.getString("best_summary"),
                        getNullableLong(rs, "runtime_millis"),
                        rs.getString("artifacts_json"),
                        rs.getString("resumed_from"),
                        rs.getString("error_message"),
                        rs.getString("config_yaml"),
                        rs.getString("config_json"),
                        rs.getString("created_at")
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed reading run " + runId, e);
        }
    }

    @Override
    public List<IterationMetric> listIterations(String runId) {
        String sql = """
                SELECT iteration, evaluations, best_fitness, mean_fitness, std_fitness, metrics_json, diagnostics_json, created_at
                FROM iterations
                WHERE run_id = ?
                ORDER BY iteration ASC
                """;
        List<IterationMetric> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, runId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new IterationMetric(
                            rs.getInt("iteration"),
                            rs.getLong("evaluations"),
                            rs.getDouble("best_fitness"),
                            rs.getDouble("mean_fitness"),
                            rs.getDouble("std_fitness"),
                            rs.getString("metrics_json"),
                            rs.getString("diagnostics_json"),
                            rs.getString("created_at")
                    ));
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed listing iterations for run " + runId, e);
        }
    }

    @Override
    public List<CheckpointRow> listCheckpoints(String runId) {
        String sql = """
                SELECT id, run_id, iteration, checkpoint_path, created_at
                FROM checkpoints
                WHERE run_id = ?
                ORDER BY iteration ASC
                """;
        List<CheckpointRow> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, runId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new CheckpointRow(
                            rs.getLong("id"),
                            rs.getString("run_id"),
                            rs.getInt("iteration"),
                            rs.getString("checkpoint_path"),
                            rs.getString("created_at")
                    ));
                }
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed listing checkpoints for run " + runId, e);
        }
    }

    @Override
    public PageResult<EventRow> listEvents(String runId, String eventType, String q, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 200));
        int offset = safePage * safeSize;

        StringBuilder where = new StringBuilder(" WHERE run_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(runId);

        if (hasText(eventType)) {
            where.append(" AND LOWER(event_type) = ? ");
            params.add(eventType.toLowerCase(Locale.ROOT));
        }
        if (hasText(q)) {
            where.append(" AND (LOWER(event_type) LIKE ? OR LOWER(payload_json) LIKE ?) ");
            String like = like(q);
            params.add(like);
            params.add(like);
        }

        String countSql = "SELECT COUNT(*) FROM events " + where;
        String dataSql = """
                SELECT id, run_id, event_type, payload_json, created_at
                FROM events
                """
                + where
                + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";

        try (Connection connection = dataSource.getConnection()) {
            long total = queryCount(connection, countSql, params);

            List<Object> dataParams = new ArrayList<>(params);
            dataParams.add(safeSize);
            dataParams.add(offset);

            List<EventRow> rows = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(dataSql)) {
                bindParams(statement, dataParams);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new EventRow(
                                rs.getLong("id"),
                                rs.getString("run_id"),
                                rs.getString("event_type"),
                                rs.getString("payload_json"),
                                rs.getString("created_at")
                        ));
                    }
                }
            }

            long totalPages = total == 0 ? 0 : ((total + safeSize - 1) / safeSize);
            return new PageResult<>(rows, safePage, safeSize, total, totalPages);
        } catch (Exception e) {
            throw new RuntimeException("Failed listing events for run " + runId, e);
        }
    }

    @Override
    public List<ExperimentParamRow> listExperimentParams(String runId) {
        String sql = """
                SELECT
                    ep.id,
                    ep.experiment_id,
                    ep.section,
                    ep.param_path,
                    ep.leaf_key,
                    ep.value_type,
                    ep.value_text,
                    ep.value_number,
                    ep.value_boolean,
                    ep.value_json
                FROM experiment_params ep
                JOIN runs r ON r.experiment_id = ep.experiment_id
                WHERE r.run_id = ?
                ORDER BY ep.section ASC, ep.param_path ASC
                """;
        List<ExperimentParamRow> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, runId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ExperimentParamRow(
                            rs.getLong("id"),
                            rs.getString("experiment_id"),
                            rs.getString("section"),
                            rs.getString("param_path"),
                            rs.getString("leaf_key"),
                            rs.getString("value_type"),
                            rs.getString("value_text"),
                            getNullableDouble(rs, "value_number"),
                            getNullableInteger(rs, "value_boolean"),
                            rs.getString("value_json")
                    ));
                }
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed listing experiment params for run " + runId, e);
        }
    }

    @Override
    public FilterFacets listFacets() {
        try (Connection connection = dataSource.getConnection()) {
            List<String> algorithms = queryDistinct(connection, "SELECT DISTINCT algorithm_type FROM experiments ORDER BY algorithm_type");
            List<String> models = queryDistinct(connection, "SELECT DISTINCT model_type FROM experiments ORDER BY model_type");
            List<String> problems = queryDistinct(connection, "SELECT DISTINCT problem_type FROM experiments ORDER BY problem_type");
            List<String> statuses = queryDistinct(connection, "SELECT DISTINCT status FROM runs ORDER BY status");
            return new FilterFacets(algorithms, models, problems, statuses);
        } catch (Exception e) {
            throw new RuntimeException("Failed listing filter facets", e);
        }
    }

    private static RunQuery normalize(RunQuery query) {
        RunQuery base = query == null ? RunQuery.defaults() : query;
        int safePage = Math.max(0, base.page());
        int safeSize = Math.max(1, Math.min(base.size(), 200));
        String sortBy = hasText(base.sortBy()) ? base.sortBy().toLowerCase(Locale.ROOT) : "start_time";
        String sortDir = "asc".equalsIgnoreCase(base.sortDir()) ? "asc" : "desc";
        return new RunQuery(
                trimToNull(base.q()),
                trimToNull(base.algorithm()),
                trimToNull(base.model()),
                trimToNull(base.problem()),
                trimToNull(base.status()),
                trimToNull(base.from()),
                trimToNull(base.to()),
                base.minBest(),
                base.maxBest(),
                safePage,
                safeSize,
                sortBy,
                sortDir
        );
    }

    private static WhereClause buildRunFilters(RunQuery query) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        if (hasText(query.algorithm())) {
            sql.append(" AND LOWER(e.algorithm_type) = ? ");
            params.add(query.algorithm().toLowerCase(Locale.ROOT));
        }
        if (hasText(query.model())) {
            sql.append(" AND LOWER(e.model_type) = ? ");
            params.add(query.model().toLowerCase(Locale.ROOT));
        }
        if (hasText(query.problem())) {
            sql.append(" AND LOWER(e.problem_type) = ? ");
            params.add(query.problem().toLowerCase(Locale.ROOT));
        }
        if (hasText(query.status())) {
            sql.append(" AND LOWER(r.status) = ? ");
            params.add(query.status().toLowerCase(Locale.ROOT));
        }
        if (hasText(query.from())) {
            sql.append(" AND r.start_time >= ? ");
            params.add(query.from());
        }
        if (hasText(query.to())) {
            sql.append(" AND r.start_time <= ? ");
            params.add(query.to());
        }
        if (query.minBest() != null) {
            sql.append(" AND r.best_fitness >= ? ");
            params.add(query.minBest());
        }
        if (query.maxBest() != null) {
            sql.append(" AND r.best_fitness <= ? ");
            params.add(query.maxBest());
        }
        if (hasText(query.q())) {
            sql.append("""
                     AND (
                        LOWER(r.run_id) LIKE ?
                        OR LOWER(e.algorithm_type) LIKE ?
                        OR LOWER(e.model_type) LIKE ?
                        OR LOWER(e.problem_type) LIKE ?
                        OR LOWER(e.config_hash) LIKE ?
                        OR LOWER(e.experiment_id) LIKE ?
                        OR EXISTS (
                            SELECT 1
                            FROM experiment_params ep
                            WHERE ep.experiment_id = e.experiment_id
                              AND (
                                LOWER(ep.param_path) LIKE ?
                                OR LOWER(COALESCE(ep.value_text, '')) LIKE ?
                                OR LOWER(COALESCE(ep.value_json, '')) LIKE ?
                              )
                        )
                     )
                    """);
            String like = like(query.q());
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        return new WhereClause(sql.toString(), params);
    }

    private static long queryCount(Connection connection, String sql, List<Object> params) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParams(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    private static List<String> queryDistinct(Connection connection, String sql) throws Exception {
        List<String> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String value = rs.getString(1);
                if (value != null && !value.isBlank()) {
                    result.add(value);
                }
            }
        }
        return result;
    }

    private static void bindParams(PreparedStatement statement, List<Object> params) throws Exception {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            int index = i + 1;
            if (value == null) {
                statement.setObject(index, null);
            } else if (value instanceof Integer integer) {
                statement.setInt(index, integer);
            } else if (value instanceof Long longValue) {
                statement.setLong(index, longValue);
            } else if (value instanceof Double doubleValue) {
                statement.setDouble(index, doubleValue);
            } else {
                statement.setString(index, value.toString());
            }
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String like(String value) {
        return "%" + value.toLowerCase(Locale.ROOT) + "%";
    }

    private static Double getNullableDouble(ResultSet rs, String column) throws Exception {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private static Long getNullableLong(ResultSet rs, String column) throws Exception {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Integer getNullableInteger(ResultSet rs, String column) throws Exception {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private record WhereClause(String sql, List<Object> params) {
    }
}
