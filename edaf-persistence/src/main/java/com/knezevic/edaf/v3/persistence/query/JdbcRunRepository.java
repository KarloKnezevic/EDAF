/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * JDBC read repository used by reporting and web dashboard.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class JdbcRunRepository implements RunRepository {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Map<String, String> SORT_COLUMNS = new LinkedHashMap<>();
    private static final Map<String, String> EXPERIMENT_SORT_COLUMNS = new LinkedHashMap<>();
    private static final Map<String, String> EXPERIMENT_RUN_SORT_COLUMNS = new LinkedHashMap<>();

    static {
        SORT_COLUMNS.put("start_time", "r.start_time");
        SORT_COLUMNS.put("best_fitness", "r.best_fitness");
        SORT_COLUMNS.put("runtime_millis", "r.runtime_millis");
        SORT_COLUMNS.put("status", "r.status");

        EXPERIMENT_SORT_COLUMNS.put("latest_run_time", "latest_run_time");
        EXPERIMENT_SORT_COLUMNS.put("created_at", "e.created_at");
        EXPERIMENT_SORT_COLUMNS.put("algorithm_type", "e.algorithm_type");
        EXPERIMENT_SORT_COLUMNS.put("model_type", "e.model_type");
        EXPERIMENT_SORT_COLUMNS.put("problem_type", "e.problem_type");
        EXPERIMENT_SORT_COLUMNS.put("total_runs", "total_runs");
        EXPERIMENT_SORT_COLUMNS.put("best_fitness", "best_fitness");

        EXPERIMENT_RUN_SORT_COLUMNS.put("start_time", "r.start_time");
        EXPERIMENT_RUN_SORT_COLUMNS.put("best_fitness", "r.best_fitness");
        EXPERIMENT_RUN_SORT_COLUMNS.put("runtime_millis", "r.runtime_millis");
        EXPERIMENT_RUN_SORT_COLUMNS.put("status", "r.status");
        EXPERIMENT_RUN_SORT_COLUMNS.put("seed", "r.seed");
    }

    private static final Set<String> MINIMIZE_PROBLEMS = Set.of(
            "sphere",
            "rosenbrock",
            "rastrigin",
            "small-tsp",
            "tsplib-tsp",
            "cec2014",
            "nguyen-sr",
            "zdt",
            "dtlz",
            "coco-bbob",
            "disjunct-matrix",
            "resolvable-matrix",
            "almost-disjunct-matrix"
    );

    private final DataSource dataSource;

    /**
     * Creates a new JdbcRunRepository instance.
     *
     * @param dataSource jdbc data source
     */
    public JdbcRunRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Lists experiment summaries.
     *
     * @param query the query argument
     * @return paged experiment list
     */
    @Override
    public PageResult<ExperimentListItem> listExperiments(ExperimentQuery query) {
        ExperimentQuery effective = normalize(query);
        WhereClause filters = buildExperimentFilters(effective);
        String sortColumn = EXPERIMENT_SORT_COLUMNS.getOrDefault(
                effective.sortBy(),
                EXPERIMENT_SORT_COLUMNS.get("latest_run_time")
        );
        String sortDir = "asc".equalsIgnoreCase(effective.sortDir()) ? "ASC" : "DESC";

        String fromSql = """
                FROM experiments e
                LEFT JOIN runs r ON r.experiment_id = e.experiment_id
                WHERE 1 = 1
                """;
        String whereSql = fromSql + filters.sql();

        String countSql = "SELECT COUNT(*) FROM (SELECT e.experiment_id " + whereSql + " GROUP BY e.experiment_id)";
        String dataSql = """
                SELECT
                    e.experiment_id,
                    e.run_name,
                    e.algorithm_type,
                    e.model_type,
                    e.problem_type,
                    e.representation_type,
                    e.config_hash,
                    e.created_at,
                    COALESCE(MAX(r.start_time), e.created_at) AS latest_run_time,
                    COUNT(r.run_id) AS total_runs,
                    SUM(CASE WHEN LOWER(COALESCE(r.status, '')) = 'completed' THEN 1 ELSE 0 END) AS completed_runs,
                    SUM(CASE WHEN LOWER(COALESCE(r.status, '')) IN ('failed', 'stopped') THEN 1 ELSE 0 END) AS failed_runs,
                    SUM(CASE WHEN LOWER(COALESCE(r.status, '')) = 'running' THEN 1 ELSE 0 END) AS running_runs,
                    MAX(r.best_fitness) AS best_fitness
                """
                + whereSql
                + """
                GROUP BY
                    e.experiment_id,
                    e.run_name,
                    e.algorithm_type,
                    e.model_type,
                    e.problem_type,
                    e.representation_type,
                    e.config_hash,
                    e.created_at
                ORDER BY
                """
                + sortColumn + " " + sortDir + ", e.experiment_id ASC"
                + " LIMIT ? OFFSET ?";

        try (Connection connection = dataSource.getConnection()) {
            long total = queryCount(connection, countSql, filters.params());

            List<Object> dataParams = new ArrayList<>(filters.params());
            dataParams.add(effective.size());
            dataParams.add(effective.offset());

            List<ExperimentListItem> items = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(dataSql)) {
                bindParams(statement, dataParams);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Long totalRuns = getNullableLong(rs, "total_runs");
                        Long completedRuns = getNullableLong(rs, "completed_runs");
                        Long failedRuns = getNullableLong(rs, "failed_runs");
                        Long runningRuns = getNullableLong(rs, "running_runs");
                        items.add(new ExperimentListItem(
                                rs.getString("experiment_id"),
                                rs.getString("run_name"),
                                rs.getString("algorithm_type"),
                                rs.getString("model_type"),
                                rs.getString("problem_type"),
                                rs.getString("representation_type"),
                                rs.getString("config_hash"),
                                rs.getString("created_at"),
                                rs.getString("latest_run_time"),
                                totalRuns,
                                completedRuns,
                                failedRuns,
                                runningRuns,
                                deriveExperimentStatus(totalRuns, completedRuns, failedRuns, runningRuns),
                                getNullableDouble(rs, "best_fitness")
                        ));
                    }
                }
            }

            long totalPages = total == 0 ? 0 : ((total + effective.size() - 1) / effective.size());
            return new PageResult<>(items, effective.page(), effective.size(), total, totalPages);
        } catch (Exception e) {
            throw new RuntimeException("Failed listing experiments", e);
        }
    }

    /**
     * Lists run summaries.
     *
     * @param query the query argument
     * @return paged run list
     */
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

    /**
     * Executes get run detail.
     *
     * @param runId run identifier
     * @return the run detail
     */
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

    /**
     * Lists iterations.
     *
     * @param runId run identifier
     * @return the list iterations
     */
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

    /**
     * Lists checkpoints.
     *
     * @param runId run identifier
     * @return the list checkpoints
     */
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

    /**
     * Lists events.
     *
     * @param runId run identifier
     * @param eventType the eventType argument
     * @param q search query
     * @param page page index
     * @param size page size
     * @return the list events
     */
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

    /**
     * Lists experiment params.
     *
     * @param runId run identifier
     * @return the list experiment params
     */
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

    /**
     * Lists facets.
     *
     * @return the list facets
     */
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

    /**
     * Executes get experiment detail.
     *
     * @param experimentId experiment identifier
     * @return the experiment detail
     */
    @Override
    public ExperimentDetail getExperimentDetail(String experimentId) {
        String sql = """
                SELECT
                    e.experiment_id,
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
                    e.config_yaml,
                    e.config_json,
                    e.created_at,
                    COUNT(r.run_id) AS total_runs,
                    COALESCE(SUM(CASE WHEN r.status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completed_runs,
                    COALESCE(SUM(CASE WHEN r.status IN ('FAILED', 'STOPPED') THEN 1 ELSE 0 END), 0) AS failed_runs,
                    COALESCE(SUM(CASE WHEN r.status = 'RUNNING' THEN 1 ELSE 0 END), 0) AS running_runs
                FROM experiments e
                LEFT JOIN runs r ON r.experiment_id = e.experiment_id
                WHERE e.experiment_id = ?
                GROUP BY
                    e.experiment_id,
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
                    e.config_yaml,
                    e.config_json,
                    e.created_at
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, experimentId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ExperimentDetail(
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
                        rs.getString("config_yaml"),
                        rs.getString("config_json"),
                        rs.getString("created_at"),
                        rs.getLong("total_runs"),
                        rs.getLong("completed_runs"),
                        rs.getLong("failed_runs"),
                        rs.getLong("running_runs")
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed reading experiment " + experimentId, e);
        }
    }

    /**
     * Lists experiment runs.
     *
     * @param experimentId experiment identifier
     * @param page page index
     * @param size page size
     * @param sortBy sort field
     * @param sortDir sort direction
     * @return the list experiment runs
     */
    @Override
    public PageResult<ExperimentRunItem> listExperimentRuns(String experimentId, int page, int size, String sortBy, String sortDir) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 200));
        int offset = safePage * safeSize;

        String sortKey = hasText(sortBy) ? sortBy.toLowerCase(Locale.ROOT) : "start_time";
        String sortColumn = EXPERIMENT_RUN_SORT_COLUMNS.getOrDefault(sortKey, "r.start_time");
        String sortDirection = "asc".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";

        String countSql = "SELECT COUNT(*) FROM runs r WHERE r.experiment_id = ?";
        String dataSql = """
                SELECT
                    r.run_id,
                    r.seed,
                    r.status,
                    r.start_time,
                    r.end_time,
                    r.iterations,
                    r.evaluations,
                    r.best_fitness,
                    r.runtime_millis,
                    r.resumed_from,
                    r.error_message
                FROM runs r
                WHERE r.experiment_id = ?
                """ + " ORDER BY " + sortColumn + " " + sortDirection + " LIMIT ? OFFSET ?";

        try (Connection connection = dataSource.getConnection()) {
            long total;
            try (PreparedStatement count = connection.prepareStatement(countSql)) {
                count.setString(1, experimentId);
                try (ResultSet rs = count.executeQuery()) {
                    total = rs.next() ? rs.getLong(1) : 0L;
                }
            }

            List<ExperimentRunItem> rows = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(dataSql)) {
                statement.setString(1, experimentId);
                statement.setInt(2, safeSize);
                statement.setInt(3, offset);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new ExperimentRunItem(
                                rs.getString("run_id"),
                                rs.getLong("seed"),
                                rs.getString("status"),
                                rs.getString("start_time"),
                                rs.getString("end_time"),
                                getNullableInteger(rs, "iterations"),
                                getNullableLong(rs, "evaluations"),
                                getNullableDouble(rs, "best_fitness"),
                                getNullableLong(rs, "runtime_millis"),
                                rs.getString("resumed_from"),
                                rs.getString("error_message")
                        ));
                    }
                }
            }

            long totalPages = total == 0 ? 0 : ((total + safeSize - 1) / safeSize);
            return new PageResult<>(rows, safePage, safeSize, total, totalPages);
        } catch (Exception e) {
            throw new RuntimeException("Failed listing experiment runs for " + experimentId, e);
        }
    }

    /**
     * Executes analyze experiment.
     *
     * @param experimentId experiment identifier
     * @param objectiveDirection the objectiveDirection argument
     * @param targetFitness the targetFitness argument
     * @return the analyze experiment
     */
    @Override
    public ExperimentAnalytics analyzeExperiment(String experimentId, String objectiveDirection, Double targetFitness) {
        ExperimentDetail detail = getExperimentDetail(experimentId);
        if (detail == null) {
            return new ExperimentAnalytics(
                    experimentId,
                    resolveDirectionLabel(objectiveDirection, null),
                    targetFitness,
                    targetFitness == null ? "none" : "query",
                    0,
                    0,
                    0,
                    0.0,
                    null,
                    null,
                    new BoxPlotStats(null, null, null, null, null, null, null),
                    List.of(),
                    new BoxPlotStats(null, null, null, null, null, null, null),
                    new BoxPlotStats(null, null, null, null, null, null, null),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        ResolvedTarget resolvedTarget = resolveTargetFitness(detail.configJson(), targetFitness);
        Double effectiveTarget = resolvedTarget.value();
        List<RunValueRow> rows = loadRunValuesForExperiment(experimentId);
        Map<String, List<IterationPoint>> tracesByRun = loadRunTracesForExperiment(experimentId);
        boolean minimize = isMinimize(objectiveDirection, detail.problemType());
        List<RunOutcome> outcomes = new ArrayList<>(rows.size());
        for (RunValueRow row : rows) {
            List<IterationPoint> trace = tracesByRun.getOrDefault(row.runId(), List.of());
            Long evalToTarget = firstTargetHitEvaluation(trace, row, minimize, effectiveTarget);
            long evaluationBudget = resolveEvaluationBudget(row, trace);
            if (evalToTarget == null && effectiveTarget == null && "COMPLETED".equalsIgnoreCase(row.status()) && evaluationBudget > 0L) {
                // Legacy fallback: without explicit target, treat completed runs as successful at final budget.
                evalToTarget = evaluationBudget;
            }
            boolean success = effectiveTarget == null
                    ? "COMPLETED".equalsIgnoreCase(row.status())
                    : evalToTarget != null;
            outcomes.add(new RunOutcome(
                    row.runId(),
                    row.status(),
                    row.bestFitness(),
                    row.runtimeMillis(),
                    evaluationBudget,
                    evalToTarget,
                    success
            ));
        }

        long totalRuns = outcomes.size();
        long completedRuns = outcomes.stream().filter(r -> "COMPLETED".equalsIgnoreCase(r.status())).count();
        long successfulRuns = outcomes.stream().filter(RunOutcome::successful).count();
        double successRate = totalRuns == 0 ? 0.0 : successfulRuns / (double) totalRuns;

        List<Double> bestValues = outcomes.stream()
                .filter(r -> "COMPLETED".equalsIgnoreCase(r.status()) && r.bestFitness() != null)
                .map(RunOutcome::bestFitness)
                .toList();
        List<Double> runtimeValues = outcomes.stream()
                .filter(r -> r.runtimeMillis() != null)
                .map(r -> r.runtimeMillis().doubleValue())
                .toList();
        List<Double> evaluationValues = outcomes.stream()
                .filter(r -> r.evaluationBudget() > 0L)
                .map(r -> (double) r.evaluationBudget())
                .toList();

        Double ert = computeErtFromOutcomes(outcomes, successfulRuns);
        Double sp1 = computeSp1FromOutcomes(outcomes, successRate);
        List<ProfilePoint> successVsBudget = buildSuccessVsBudget(outcomes);
        List<ProfilePoint> ecdfTotal = buildEcdfTotal(outcomes);
        List<ProfilePoint> ecdfSuccessful = buildEcdfSuccessful(outcomes);
        List<HistogramBin> timeToTargetHistogram = buildTimeToTargetHistogram(outcomes);
        List<ConfidenceBandPoint> convergence95Ci = buildConvergenceWithCi(outcomes, tracesByRun);
        List<ProfilePoint> dataProfile = successVsBudget;
        List<ProfilePoint> performanceProfile = buildSinglePerformanceProfileFromOutcomes(outcomes);

        return new ExperimentAnalytics(
                experimentId,
                minimize ? "min" : "max",
                effectiveTarget,
                resolvedTarget.source(),
                totalRuns,
                completedRuns,
                successfulRuns,
                successRate,
                ert,
                sp1,
                StatisticsUtils.boxPlot(bestValues),
                bestValues,
                StatisticsUtils.boxPlot(runtimeValues),
                StatisticsUtils.boxPlot(evaluationValues),
                convergence95Ci,
                successVsBudget,
                timeToTargetHistogram,
                ecdfTotal,
                ecdfSuccessful,
                dataProfile,
                performanceProfile
        );
    }

    @Override
    public ProblemComparisonReport compareAlgorithmsOnProblem(String problemType,
                                                              String objectiveDirection,
                                                              Double targetFitness,
                                                              List<String> algorithms) {
        if (!hasText(problemType)) {
            return new ProblemComparisonReport(
                    problemType,
                    resolveDirectionLabel(objectiveDirection, problemType),
                    targetFitness,
                    List.of(),
                    List.of(),
                    new FriedmanTestResult(0, 0, null, null, List.of()),
                    List.of(),
                    List.of()
            );
        }

        List<String> requestedAlgorithms = algorithms == null
                ? List.of()
                : algorithms.stream().filter(JdbcRunRepository::hasText).map(String::trim).toList();

        List<RunValueRow> rows = loadRunValuesForProblem(problemType, requestedAlgorithms);
        boolean minimize = isMinimize(objectiveDirection, problemType);

        Map<String, List<RunValueRow>> byAlgorithm = new LinkedHashMap<>();
        for (RunValueRow row : rows) {
            byAlgorithm.computeIfAbsent(row.algorithmType(), key -> new ArrayList<>()).add(row);
        }

        List<AlgorithmComparisonRow> summaries = new ArrayList<>();
        for (Map.Entry<String, List<RunValueRow>> entry : byAlgorithm.entrySet()) {
            Predicate<RunValueRow> success = row -> isSuccessful(row, minimize, targetFitness);
            List<RunValueRow> algorithmRows = entry.getValue();
            long totalRuns = algorithmRows.size();
            long completedRuns = algorithmRows.stream().filter(r -> "COMPLETED".equalsIgnoreCase(r.status())).count();
            long successfulRuns = algorithmRows.stream().filter(success).count();
            double successRate = totalRuns == 0 ? 0.0 : successfulRuns / (double) totalRuns;
            List<Double> best = algorithmRows.stream()
                    .filter(r -> "COMPLETED".equalsIgnoreCase(r.status()) && r.bestFitness() != null)
                    .map(RunValueRow::bestFitness)
                    .toList();

            summaries.add(new AlgorithmComparisonRow(
                    entry.getKey(),
                    totalRuns,
                    completedRuns,
                    successfulRuns,
                    successRate,
                    StatisticsUtils.mean(best),
                    StatisticsUtils.quantile(best, 0.5),
                    StatisticsUtils.stdDev(best),
                    computeErt(algorithmRows, successfulRuns),
                    computeSp1(algorithmRows, success, successRate)
            ));
        }

        summaries.sort(Comparator.comparing(AlgorithmComparisonRow::algorithm));
        List<PairwiseTestResult> pairwise = buildPairwiseComparisons(byAlgorithm, minimize);
        FriedmanTestResult friedman = buildFriedman(byAlgorithm, minimize);
        List<ProfileSeries> dataProfiles = buildDataProfiles(byAlgorithm, minimize, targetFitness);
        List<ProfileSeries> performanceProfiles = buildPerformanceProfiles(byAlgorithm);

        return new ProblemComparisonReport(
                problemType,
                minimize ? "min" : "max",
                targetFitness,
                summaries,
                pairwise,
                friedman,
                dataProfiles,
                performanceProfiles
        );
    }

    /**
     * Lists run ids for experiment.
     *
     * @param experimentId experiment identifier
     * @return the list run ids for experiment
     */
    @Override
    public List<String> listRunIdsForExperiment(String experimentId) {
        if (!hasText(experimentId)) {
            return List.of();
        }
        String sql = """
                SELECT run_id
                FROM runs
                WHERE experiment_id = ?
                ORDER BY start_time ASC, run_id ASC
                """;
        List<String> runIds = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, experimentId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    runIds.add(rs.getString("run_id"));
                }
            }
            return runIds;
        } catch (Exception e) {
            throw new RuntimeException("Failed listing run ids for experiment " + experimentId, e);
        }
    }

    /**
     * Executes delete experiment.
     *
     * @param experimentId experiment identifier
     * @return the delete experiment
     */
    @Override
    public ExperimentDeletionResult deleteExperiment(String experimentId) {
        if (!hasText(experimentId)) {
            return new ExperimentDeletionResult(
                    experimentId,
                    false,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
            );
        }
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int runObjectivesDeleted = executeDelete(connection, """
                        DELETE FROM run_objectives
                        WHERE run_id IN (SELECT run_id FROM runs WHERE experiment_id = ?)
                        """, experimentId);
                int iterationsDeleted = executeDelete(connection, """
                        DELETE FROM iterations
                        WHERE run_id IN (SELECT run_id FROM runs WHERE experiment_id = ?)
                        """, experimentId);
                int checkpointsDeleted = executeDelete(connection, """
                        DELETE FROM checkpoints
                        WHERE run_id IN (SELECT run_id FROM runs WHERE experiment_id = ?)
                        """, experimentId);
                int eventsDeleted = executeDelete(connection, """
                        DELETE FROM events
                        WHERE run_id IN (SELECT run_id FROM runs WHERE experiment_id = ?)
                        """, experimentId);
                executeDelete(connection, """
                        DELETE FROM control_requests
                        WHERE (scope = 'run' AND target_id IN (SELECT run_id FROM runs WHERE experiment_id = ?))
                           OR (scope = 'experiment' AND target_id = ?)
                        """, experimentId, experimentId);
                int runsDeleted = executeDelete(connection, """
                        DELETE FROM runs
                        WHERE experiment_id = ?
                        """, experimentId);
                int paramsDeleted = executeDelete(connection, """
                        DELETE FROM experiment_params
                        WHERE experiment_id = ?
                        """, experimentId);
                int experimentsDeleted = executeDelete(connection, """
                        DELETE FROM experiments
                        WHERE experiment_id = ?
                        """, experimentId);

                connection.commit();
                return new ExperimentDeletionResult(
                        experimentId,
                        experimentsDeleted > 0,
                        runsDeleted,
                        runObjectivesDeleted,
                        iterationsDeleted,
                        checkpointsDeleted,
                        eventsDeleted,
                        paramsDeleted
                );
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed deleting experiment " + experimentId, e);
        }
    }

    /**
     * Executes request run stop.
     *
     * @param runId run identifier
     * @param requestedBy the requestedBy argument
     * @param reason the reason argument
     * @return the request run stop
     */
    @Override
    public StopRequestResult requestRunStop(String runId, String requestedBy, String reason) {
        String normalizedRunId = trimToNull(runId);
        if (!hasText(normalizedRunId)) {
            return new StopRequestResult("run", runId, false, false, 0, "Run id is required.");
        }

        String runSql = """
                SELECT run_id, status
                FROM runs
                WHERE run_id = ?
                """;
        try (Connection connection = dataSource.getConnection()) {
            String status = null;
            try (PreparedStatement statement = connection.prepareStatement(runSql)) {
                statement.setString(1, normalizedRunId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        return new StopRequestResult("run", normalizedRunId, false, false, 0,
                                "Run not found: " + normalizedRunId);
                    }
                    status = rs.getString("status");
                }
            }

            if (!"RUNNING".equalsIgnoreCase(status)) {
                return new StopRequestResult("run", normalizedRunId, true, false, 0,
                        "Run is not RUNNING (status=" + safeStatus(status) + ").");
            }

            upsertStopRequest(connection, "run", normalizedRunId, requestedBy, reason);
            return new StopRequestResult("run", normalizedRunId, true, true, 1,
                    "Stop requested for run " + normalizedRunId + ".");
        } catch (Exception e) {
            throw new RuntimeException("Failed requesting run stop for " + normalizedRunId, e);
        }
    }

    /**
     * Executes request experiment stop.
     *
     * @param experimentId experiment identifier
     * @param requestedBy the requestedBy argument
     * @param reason the reason argument
     * @return the request experiment stop
     */
    @Override
    public StopRequestResult requestExperimentStop(String experimentId, String requestedBy, String reason) {
        String normalizedExperimentId = trimToNull(experimentId);
        if (!hasText(normalizedExperimentId)) {
            return new StopRequestResult("experiment", experimentId, false, false, 0, "Experiment id is required.");
        }

        try (Connection connection = dataSource.getConnection()) {
            if (!exists(connection, "SELECT 1 FROM experiments WHERE experiment_id = ?", normalizedExperimentId)) {
                return new StopRequestResult("experiment", normalizedExperimentId, false, false, 0,
                        "Experiment not found: " + normalizedExperimentId);
            }

            upsertStopRequest(connection, "experiment", normalizedExperimentId, requestedBy, reason);
            int running = upsertRunStopRequestsForExperiment(connection, normalizedExperimentId, requestedBy, reason);
            String message = running > 0
                    ? "Stop requested for experiment " + normalizedExperimentId + " (" + running + " running runs)."
                    : "Stop request recorded for experiment " + normalizedExperimentId + " (no currently running runs).";
            return new StopRequestResult("experiment", normalizedExperimentId, true, true, running, message);
        } catch (Exception e) {
            throw new RuntimeException("Failed requesting experiment stop for " + normalizedExperimentId, e);
        }
    }

    private List<RunValueRow> loadRunValuesForExperiment(String experimentId) {
        String sql = """
                SELECT
                    r.run_id,
                    e.algorithm_type,
                    e.problem_type,
                    r.seed,
                    r.status,
                    r.best_fitness,
                    r.evaluations,
                    r.runtime_millis
                FROM runs r
                JOIN experiments e ON e.experiment_id = r.experiment_id
                WHERE r.experiment_id = ?
                ORDER BY r.start_time ASC
                """;
        List<RunValueRow> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, experimentId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new RunValueRow(
                            rs.getString("run_id"),
                            rs.getString("algorithm_type"),
                            rs.getString("problem_type"),
                            rs.getLong("seed"),
                            rs.getString("status"),
                            getNullableDouble(rs, "best_fitness"),
                            getNullableLong(rs, "evaluations"),
                            getNullableLong(rs, "runtime_millis")
                    ));
                }
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed loading run values for experiment " + experimentId, e);
        }
    }

    private Map<String, List<IterationPoint>> loadRunTracesForExperiment(String experimentId) {
        String sql = """
                SELECT
                    r.run_id,
                    i.evaluations,
                    i.best_fitness
                FROM runs r
                LEFT JOIN iterations i ON i.run_id = r.run_id
                WHERE r.experiment_id = ?
                ORDER BY r.run_id ASC, i.evaluations ASC, i.iteration ASC
                """;
        Map<String, List<IterationPoint>> traces = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, experimentId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String runId = rs.getString("run_id");
                    traces.computeIfAbsent(runId, ignored -> new ArrayList<>());
                    long evaluations = rs.getLong("evaluations");
                    boolean hasPoint = !rs.wasNull();
                    if (hasPoint) {
                        traces.get(runId).add(new IterationPoint(
                                evaluations,
                                rs.getDouble("best_fitness")
                        ));
                    }
                }
            }
            return traces;
        } catch (Exception e) {
            throw new RuntimeException("Failed loading run traces for experiment " + experimentId, e);
        }
    }

    private static ResolvedTarget resolveTargetFitness(String configJson, Double requestedTarget) {
        if (requestedTarget != null && Double.isFinite(requestedTarget)) {
            return new ResolvedTarget(requestedTarget, "query");
        }
        if (!hasText(configJson)) {
            return new ResolvedTarget(null, "none");
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(configJson);
            Double value = findNumeric(root, "stopping", "targetFitness");
            if (value == null) {
                value = findNumeric(root, "stopping", "target");
            }
            if (value == null) {
                value = findNumeric(root, "targetFitness");
            }
            if (value == null) {
                value = findNumeric(root, "target");
            }
            if (value == null) {
                value = findNumericRecursive(root, Set.of("targetFitness", "target"));
            }
            if (value != null && Double.isFinite(value)) {
                return new ResolvedTarget(value, "config");
            }
        } catch (Exception ignored) {
            // If config JSON is malformed we silently fall back to legacy behavior.
        }
        return new ResolvedTarget(null, "none");
    }

    private static Double findNumeric(JsonNode root, String... path) {
        if (root == null || path == null || path.length == 0) {
            return null;
        }
        JsonNode cursor = root;
        for (String segment : path) {
            if (cursor == null) {
                return null;
            }
            cursor = cursor.get(segment);
        }
        if (cursor == null) {
            return null;
        }
        if (cursor.isNumber()) {
            return cursor.doubleValue();
        }
        if (cursor.isTextual()) {
            try {
                return Double.parseDouble(cursor.textValue());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Double findNumericRecursive(JsonNode node, Set<String> keys) {
        if (node == null || keys == null || keys.isEmpty()) {
            return null;
        }
        if (node.isObject()) {
            for (String key : keys) {
                JsonNode child = node.get(key);
                if (child != null) {
                    Double candidate = parseNumericNode(child);
                    if (candidate != null) {
                        return candidate;
                    }
                }
            }
            var fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                Double nested = findNumericRecursive(entry.getValue(), keys);
                if (nested != null) {
                    return nested;
                }
            }
            return null;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                Double nested = findNumericRecursive(child, keys);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static Double parseNumericNode(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isNumber()) {
            return node.doubleValue();
        }
        if (node.isTextual()) {
            try {
                return Double.parseDouble(node.textValue());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static long resolveEvaluationBudget(RunValueRow row, List<IterationPoint> trace) {
        if (row.evaluations() != null && row.evaluations() > 0L) {
            return row.evaluations();
        }
        if (trace == null || trace.isEmpty()) {
            return 0L;
        }
        return trace.getLast().evaluations();
    }

    private static Long firstTargetHitEvaluation(List<IterationPoint> trace,
                                                 RunValueRow row,
                                                 boolean minimize,
                                                 Double targetFitness) {
        if (targetFitness == null) {
            return null;
        }
        if (trace != null) {
            for (IterationPoint point : trace) {
                if (targetReached(point.bestFitness(), minimize, targetFitness)) {
                    return point.evaluations();
                }
            }
        }
        if ("COMPLETED".equalsIgnoreCase(row.status())
                && row.bestFitness() != null
                && row.evaluations() != null
                && targetReached(row.bestFitness(), minimize, targetFitness)) {
            return row.evaluations();
        }
        return null;
    }

    private static boolean targetReached(double fitness, boolean minimize, double targetFitness) {
        return minimize ? fitness <= targetFitness : fitness >= targetFitness;
    }

    private static List<ProfilePoint> buildSuccessVsBudget(List<RunOutcome> outcomes) {
        if (outcomes == null || outcomes.isEmpty()) {
            return List.of();
        }
        long maxBudget = outcomes.stream().mapToLong(RunOutcome::evaluationBudget).max().orElse(0L);
        if (maxBudget <= 0L) {
            return List.of();
        }
        List<Long> budgets = logarithmicBudgets(1L, maxBudget, 28);
        List<ProfilePoint> points = new ArrayList<>(budgets.size());
        for (long budget : budgets) {
            long solved = outcomes.stream()
                    .filter(outcome -> outcome.evaluationToTarget() != null && outcome.evaluationToTarget() <= budget)
                    .count();
            points.add(new ProfilePoint(budget, solved / (double) outcomes.size()));
        }
        return points;
    }

    private static List<ProfilePoint> buildEcdfTotal(List<RunOutcome> outcomes) {
        if (outcomes == null || outcomes.isEmpty()) {
            return List.of();
        }
        List<Long> successful = outcomes.stream()
                .map(RunOutcome::evaluationToTarget)
                .filter(value -> value != null && value > 0L)
                .sorted()
                .toList();
        if (successful.isEmpty()) {
            return List.of();
        }
        List<ProfilePoint> points = new ArrayList<>();
        long solved = 0L;
        long previous = Long.MIN_VALUE;
        for (Long evaluation : successful) {
            solved++;
            if (evaluation != previous) {
                points.add(new ProfilePoint(evaluation, solved / (double) outcomes.size()));
                previous = evaluation;
            } else {
                points.set(points.size() - 1, new ProfilePoint(evaluation, solved / (double) outcomes.size()));
            }
        }
        return points;
    }

    private static List<ProfilePoint> buildEcdfSuccessful(List<RunOutcome> outcomes) {
        if (outcomes == null || outcomes.isEmpty()) {
            return List.of();
        }
        List<Long> successful = outcomes.stream()
                .map(RunOutcome::evaluationToTarget)
                .filter(value -> value != null && value > 0L)
                .sorted()
                .toList();
        if (successful.isEmpty()) {
            return List.of();
        }
        List<ProfilePoint> points = new ArrayList<>();
        long solved = 0L;
        long previous = Long.MIN_VALUE;
        int denominator = successful.size();
        for (Long evaluation : successful) {
            solved++;
            if (evaluation != previous) {
                points.add(new ProfilePoint(evaluation, solved / (double) denominator));
                previous = evaluation;
            } else {
                points.set(points.size() - 1, new ProfilePoint(evaluation, solved / (double) denominator));
            }
        }
        return points;
    }

    private static List<HistogramBin> buildTimeToTargetHistogram(List<RunOutcome> outcomes) {
        if (outcomes == null || outcomes.isEmpty()) {
            return List.of();
        }
        List<Double> values = outcomes.stream()
                .map(RunOutcome::evaluationToTarget)
                .filter(value -> value != null && value > 0L)
                .map(value -> value.doubleValue())
                .sorted()
                .toList();
        if (values.isEmpty()) {
            return List.of();
        }
        double min = values.getFirst();
        double max = values.getLast();
        if (Double.compare(min, max) == 0) {
            return List.of(new HistogramBin(min, min + 1.0, values.size()));
        }
        int binCount = Math.max(6, Math.min(16, (int) Math.ceil(Math.sqrt(values.size()))));
        double width = (max - min) / binCount;
        long[] counts = new long[binCount];
        for (double value : values) {
            int index = Math.min(binCount - 1, (int) Math.floor((value - min) / width));
            counts[index] += 1L;
        }
        List<HistogramBin> histogram = new ArrayList<>(binCount);
        for (int index = 0; index < binCount; index++) {
            double start = min + index * width;
            double end = index == binCount - 1 ? max + 1.0 : start + width;
            histogram.add(new HistogramBin(start, end, counts[index]));
        }
        return histogram;
    }

    private static List<ConfidenceBandPoint> buildConvergenceWithCi(List<RunOutcome> outcomes,
                                                                    Map<String, List<IterationPoint>> tracesByRun) {
        if (outcomes == null || outcomes.isEmpty() || tracesByRun == null || tracesByRun.isEmpty()) {
            return List.of();
        }
        long maxBudget = outcomes.stream().mapToLong(RunOutcome::evaluationBudget).max().orElse(0L);
        if (maxBudget <= 0L) {
            return List.of();
        }
        List<Long> grid = linearBudgets(maxBudget, 64);
        List<ConfidenceBandPoint> points = new ArrayList<>(grid.size());
        for (long budget : grid) {
            List<Double> sample = new ArrayList<>(outcomes.size());
            for (RunOutcome outcome : outcomes) {
                List<IterationPoint> trace = tracesByRun.get(outcome.runId());
                Double fitness = bestFitnessAtBudget(trace, budget);
                if (fitness != null) {
                    sample.add(fitness);
                }
            }
            if (sample.isEmpty()) {
                continue;
            }
            Double mean = StatisticsUtils.mean(sample);
            Double stdDev = StatisticsUtils.stdDev(sample);
            long count = sample.size();
            double margin = (stdDev == null || count < 2) ? 0.0 : 1.96 * (stdDev / Math.sqrt(count));
            double meanValue = mean == null ? 0.0 : mean;
            points.add(new ConfidenceBandPoint(
                    budget,
                    meanValue,
                    meanValue - margin,
                    meanValue + margin,
                    StatisticsUtils.quantile(sample, 0.5),
                    count
            ));
        }
        return points;
    }

    private static Double bestFitnessAtBudget(List<IterationPoint> trace, long budget) {
        if (trace == null || trace.isEmpty()) {
            return null;
        }
        double fallback = trace.getFirst().bestFitness();
        double best = fallback;
        boolean found = false;
        for (IterationPoint point : trace) {
            if (point.evaluations() <= budget) {
                best = point.bestFitness();
                found = true;
            } else {
                break;
            }
        }
        return found ? best : fallback;
    }

    private static List<Long> linearBudgets(long maxBudget, int points) {
        if (maxBudget <= 0L || points <= 1) {
            return List.of(maxBudget);
        }
        Set<Long> values = new LinkedHashSet<>();
        for (int i = 0; i < points; i++) {
            double ratio = i / (double) (points - 1);
            long budget = Math.round(maxBudget * ratio);
            values.add(Math.max(0L, budget));
        }
        values.add(maxBudget);
        return values.stream().sorted().toList();
    }

    private static List<ProfilePoint> buildSinglePerformanceProfileFromOutcomes(List<RunOutcome> outcomes) {
        if (outcomes == null || outcomes.isEmpty()) {
            return List.of();
        }
        List<Long> successfulEvaluations = outcomes.stream()
                .map(RunOutcome::evaluationToTarget)
                .filter(value -> value != null && value > 0L)
                .toList();
        if (successfulEvaluations.isEmpty()) {
            return List.of();
        }
        long bestEvaluation = successfulEvaluations.stream().min(Long::compareTo).orElse(1L);
        double[] taus = {1.0, 1.2, 1.5, 2.0, 3.0, 5.0, 8.0, 13.0, 21.0, 34.0, 55.0, 89.0, 144.0};
        List<ProfilePoint> points = new ArrayList<>(taus.length);
        for (double tau : taus) {
            double threshold = tau * bestEvaluation;
            long solved = outcomes.stream()
                    .filter(outcome -> outcome.evaluationToTarget() != null && outcome.evaluationToTarget() <= threshold)
                    .count();
            points.add(new ProfilePoint(tau, solved / (double) outcomes.size()));
        }
        return points;
    }

    private List<RunValueRow> loadRunValuesForProblem(String problemType, List<String> requestedAlgorithms) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    r.run_id,
                    e.algorithm_type,
                    e.problem_type,
                    r.seed,
                    r.status,
                    r.best_fitness,
                    r.evaluations,
                    r.runtime_millis
                FROM runs r
                JOIN experiments e ON e.experiment_id = r.experiment_id
                WHERE LOWER(e.problem_type) = ?
                """);
        List<Object> params = new ArrayList<>();
        params.add(problemType.toLowerCase(Locale.ROOT));

        if (requestedAlgorithms != null && !requestedAlgorithms.isEmpty()) {
            sql.append(" AND LOWER(e.algorithm_type) IN (");
            for (int i = 0; i < requestedAlgorithms.size(); i++) {
                if (i > 0) {
                    sql.append(",");
                }
                sql.append("?");
                params.add(requestedAlgorithms.get(i).toLowerCase(Locale.ROOT));
            }
            sql.append(") ");
        }

        sql.append(" ORDER BY e.algorithm_type ASC, r.start_time ASC ");

        List<RunValueRow> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParams(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new RunValueRow(
                            rs.getString("run_id"),
                            rs.getString("algorithm_type"),
                            rs.getString("problem_type"),
                            rs.getLong("seed"),
                            rs.getString("status"),
                            getNullableDouble(rs, "best_fitness"),
                            getNullableLong(rs, "evaluations"),
                            getNullableLong(rs, "runtime_millis")
                    ));
                }
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed loading run values for problem " + problemType, e);
        }
    }

    private static List<ProfilePoint> buildSingleDataProfile(List<RunValueRow> rows, Predicate<RunValueRow> success) {
        if (rows.isEmpty()) {
            return List.of();
        }
        long maxEval = rows.stream().filter(r -> r.evaluations() != null).mapToLong(RunValueRow::evaluations).max().orElse(0L);
        long minEval = rows.stream().filter(r -> r.evaluations() != null).mapToLong(RunValueRow::evaluations).min().orElse(0L);
        if (maxEval <= 0L) {
            return List.of();
        }

        List<Long> budgets = logarithmicBudgets(Math.max(1L, minEval), maxEval, 24);
        List<ProfilePoint> points = new ArrayList<>(budgets.size());
        for (long budget : budgets) {
            long solved = rows.stream()
                    .filter(success)
                    .filter(r -> r.evaluations() != null && r.evaluations() <= budget)
                    .count();
            points.add(new ProfilePoint(budget, solved / (double) rows.size()));
        }
        return points;
    }

    private static List<ProfilePoint> buildSinglePerformanceProfile(List<RunValueRow> rows, Predicate<RunValueRow> success) {
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> successfulEvaluations = rows.stream()
                .filter(success)
                .filter(r -> r.evaluations() != null)
                .map(RunValueRow::evaluations)
                .toList();
        if (successfulEvaluations.isEmpty()) {
            return List.of();
        }

        long bestEvaluation = successfulEvaluations.stream().min(Long::compareTo).orElse(1L);
        double[] taus = {1.0, 1.2, 1.5, 2.0, 3.0, 5.0, 8.0, 13.0, 21.0, 34.0, 55.0, 89.0, 144.0};
        List<ProfilePoint> points = new ArrayList<>(taus.length);
        for (double tau : taus) {
            double threshold = tau * bestEvaluation;
            long solved = rows.stream()
                    .filter(success)
                    .filter(r -> r.evaluations() != null && r.evaluations() <= threshold)
                    .count();
            points.add(new ProfilePoint(tau, solved / (double) rows.size()));
        }
        return points;
    }

    private static List<ProfileSeries> buildDataProfiles(Map<String, List<RunValueRow>> byAlgorithm,
                                                         boolean minimize,
                                                         Double targetFitness) {
        if (byAlgorithm.isEmpty()) {
            return List.of();
        }

        long globalMax = byAlgorithm.values().stream()
                .flatMap(List::stream)
                .filter(r -> r.evaluations() != null)
                .mapToLong(RunValueRow::evaluations)
                .max().orElse(0L);
        long globalMin = byAlgorithm.values().stream()
                .flatMap(List::stream)
                .filter(r -> r.evaluations() != null)
                .mapToLong(RunValueRow::evaluations)
                .min().orElse(0L);
        if (globalMax <= 0L) {
            return List.of();
        }

        List<Long> budgets = logarithmicBudgets(Math.max(1L, globalMin), globalMax, 24);
        List<ProfileSeries> series = new ArrayList<>();

        for (Map.Entry<String, List<RunValueRow>> entry : byAlgorithm.entrySet()) {
            List<RunValueRow> rows = entry.getValue();
            Predicate<RunValueRow> success = row -> isSuccessful(row, minimize, targetFitness);
            List<ProfilePoint> points = new ArrayList<>(budgets.size());
            for (long budget : budgets) {
                long solved = rows.stream()
                        .filter(success)
                        .filter(r -> r.evaluations() != null && r.evaluations() <= budget)
                        .count();
                double fraction = rows.isEmpty() ? 0.0 : solved / (double) rows.size();
                points.add(new ProfilePoint(budget, fraction));
            }
            series.add(new ProfileSeries(entry.getKey(), points));
        }

        series.sort(Comparator.comparing(ProfileSeries::name));
        return series;
    }

    private static List<ProfileSeries> buildPerformanceProfiles(Map<String, List<RunValueRow>> byAlgorithm) {
        if (byAlgorithm.size() < 2) {
            return List.of();
        }

        Set<Long> seedUnion = new HashSet<>();
        for (List<RunValueRow> rows : byAlgorithm.values()) {
            rows.stream()
                    .filter(r -> "COMPLETED".equalsIgnoreCase(r.status()) && r.evaluations() != null)
                    .map(RunValueRow::seed)
                    .forEach(seedUnion::add);
        }
        if (seedUnion.isEmpty()) {
            return List.of();
        }

        Map<String, Map<Long, Long>> evalByAlgorithmAndSeed = new LinkedHashMap<>();
        for (Map.Entry<String, List<RunValueRow>> entry : byAlgorithm.entrySet()) {
            Map<Long, Long> perSeed = new HashMap<>();
            for (RunValueRow row : entry.getValue()) {
                if (!"COMPLETED".equalsIgnoreCase(row.status()) || row.evaluations() == null) {
                    continue;
                }
                perSeed.merge(row.seed(), row.evaluations(), Math::min);
            }
            evalByAlgorithmAndSeed.put(entry.getKey(), perSeed);
        }

        double[] taus = {1.0, 1.2, 1.5, 2.0, 3.0, 5.0, 8.0, 13.0, 21.0, 34.0, 55.0, 89.0, 144.0};
        List<ProfileSeries> output = new ArrayList<>();

        for (Map.Entry<String, Map<Long, Long>> entry : evalByAlgorithmAndSeed.entrySet()) {
            List<ProfilePoint> points = new ArrayList<>(taus.length);
            for (double tau : taus) {
                int success = 0;
                for (long seed : seedUnion) {
                    long best = Long.MAX_VALUE;
                    for (Map<Long, Long> perSeed : evalByAlgorithmAndSeed.values()) {
                        Long eval = perSeed.get(seed);
                        if (eval != null) {
                            best = Math.min(best, eval);
                        }
                    }
                    if (best == Long.MAX_VALUE) {
                        continue;
                    }
                    Long current = entry.getValue().get(seed);
                    if (current != null && current <= tau * best) {
                        success++;
                    }
                }
                points.add(new ProfilePoint(tau, success / (double) seedUnion.size()));
            }
            output.add(new ProfileSeries(entry.getKey(), points));
        }

        output.sort(Comparator.comparing(ProfileSeries::name));
        return output;
    }

    private static List<PairwiseTestResult> buildPairwiseComparisons(Map<String, List<RunValueRow>> byAlgorithm,
                                                                     boolean minimize) {
        List<String> algorithms = new ArrayList<>(byAlgorithm.keySet());
        algorithms.sort(String::compareTo);

        List<PairwiseDraft> drafts = new ArrayList<>();
        for (int i = 0; i < algorithms.size(); i++) {
            for (int j = i + 1; j < algorithms.size(); j++) {
                String a = algorithms.get(i);
                String b = algorithms.get(j);

                List<Double> sampleA = byAlgorithm.get(a).stream()
                        .filter(r -> "COMPLETED".equalsIgnoreCase(r.status()) && r.bestFitness() != null)
                        .map(RunValueRow::bestFitness)
                        .toList();
                List<Double> sampleB = byAlgorithm.get(b).stream()
                        .filter(r -> "COMPLETED".equalsIgnoreCase(r.status()) && r.bestFitness() != null)
                        .map(RunValueRow::bestFitness)
                        .toList();

                double pValue = StatisticsUtils.wilcoxonRankSumPValue(sampleA, sampleB);
                Double medianA = StatisticsUtils.quantile(sampleA, 0.5);
                Double medianB = StatisticsUtils.quantile(sampleB, 0.5);

                String better = "none";
                if (medianA != null && medianB != null && Double.compare(medianA, medianB) != 0) {
                    better = isABetter(medianA, medianB, minimize) ? a : b;
                }

                drafts.add(new PairwiseDraft(a, b, sampleA.size(), sampleB.size(), pValue, better));
            }
        }

        if (drafts.isEmpty()) {
            return List.of();
        }

        List<Double> rawP = drafts.stream().map(PairwiseDraft::pValue).toList();
        double[] adjusted = StatisticsUtils.holmAdjust(rawP);

        List<PairwiseTestResult> results = new ArrayList<>();
        for (int i = 0; i < drafts.size(); i++) {
            PairwiseDraft draft = drafts.get(i);
            results.add(new PairwiseTestResult(
                    draft.algorithmA(),
                    draft.algorithmB(),
                    draft.sampleSizeA(),
                    draft.sampleSizeB(),
                    draft.pValue(),
                    adjusted[i],
                    draft.betterAlgorithm(),
                    adjusted[i] < 0.05
            ));
        }
        return results;
    }

    private static int executeDelete(Connection connection, String sql, Object... params) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParams(statement, java.util.Arrays.asList(params));
            return statement.executeUpdate();
        }
    }

    private static void upsertStopRequest(Connection connection,
                                          String scope,
                                          String targetId,
                                          String requestedBy,
                                          String reason) throws Exception {
        String sql = """
                INSERT INTO control_requests(
                    scope, target_id, action, requested_at, requested_by, reason, status, acknowledged_at, acknowledged_by_run_id
                )
                VALUES (?, ?, 'STOP', ?, ?, ?, 'PENDING', NULL, NULL)
                ON CONFLICT(scope, target_id, action) DO UPDATE SET
                    requested_at = excluded.requested_at,
                    requested_by = excluded.requested_by,
                    reason = excluded.reason,
                    status = 'PENDING',
                    acknowledged_at = NULL,
                    acknowledged_by_run_id = NULL
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, scope);
            statement.setString(2, targetId);
            statement.setString(3, Instant.now().toString());
            statement.setString(4, trimToNull(requestedBy) == null ? "web-ui" : trimToNull(requestedBy));
            statement.setString(5, trimToNull(reason));
            statement.executeUpdate();
        }
    }

    private static int upsertRunStopRequestsForExperiment(Connection connection,
                                                          String experimentId,
                                                          String requestedBy,
                                                          String reason) throws Exception {
        List<String> runningRunIds = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT run_id
                FROM runs
                WHERE experiment_id = ?
                  AND UPPER(COALESCE(status, '')) = 'RUNNING'
                """)) {
            statement.setString(1, experimentId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    runningRunIds.add(rs.getString("run_id"));
                }
            }
        }

        for (String runId : runningRunIds) {
            upsertStopRequest(connection, "run", runId, requestedBy, reason);
        }
        return runningRunIds.size();
    }

    private static boolean exists(Connection connection, String sql, Object... params) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParams(statement, java.util.Arrays.asList(params));
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static String safeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "UNKNOWN";
        }
        return status;
    }

    private static FriedmanTestResult buildFriedman(Map<String, List<RunValueRow>> byAlgorithm, boolean minimize) {
        if (byAlgorithm.size() < 2) {
            return new FriedmanTestResult(0, byAlgorithm.size(), null, null, List.of());
        }

        List<String> algorithms = new ArrayList<>(byAlgorithm.keySet());
        algorithms.sort(String::compareTo);

        Map<String, Map<Long, Double>> valueByAlgorithmSeed = new LinkedHashMap<>();
        for (String algorithm : algorithms) {
            Map<Long, Double> perSeed = new HashMap<>();
            for (RunValueRow row : byAlgorithm.get(algorithm)) {
                if (!"COMPLETED".equalsIgnoreCase(row.status()) || row.bestFitness() == null) {
                    continue;
                }
                perSeed.merge(row.seed(), row.bestFitness(), (a, b) -> isABetter(a, b, minimize) ? a : b);
            }
            valueByAlgorithmSeed.put(algorithm, perSeed);
        }

        Set<Long> commonSeeds = null;
        for (Map<Long, Double> perSeed : valueByAlgorithmSeed.values()) {
            if (commonSeeds == null) {
                commonSeeds = new HashSet<>(perSeed.keySet());
            } else {
                commonSeeds.retainAll(perSeed.keySet());
            }
        }

        List<double[]> matrixRows = new ArrayList<>();
        if (commonSeeds != null && commonSeeds.size() >= 2) {
            List<Long> orderedSeeds = commonSeeds.stream().sorted().toList();
            for (Long seed : orderedSeeds) {
                double[] row = new double[algorithms.size()];
                for (int i = 0; i < algorithms.size(); i++) {
                    row[i] = valueByAlgorithmSeed.get(algorithms.get(i)).get(seed);
                }
                matrixRows.add(row);
            }
        } else {
            int minSamples = Integer.MAX_VALUE;
            Map<String, List<Double>> samples = new LinkedHashMap<>();
            for (String algorithm : algorithms) {
                List<Double> values = byAlgorithm.get(algorithm).stream()
                        .filter(r -> "COMPLETED".equalsIgnoreCase(r.status()) && r.bestFitness() != null)
                        .map(RunValueRow::bestFitness)
                        .sorted()
                        .toList();
                samples.put(algorithm, values);
                minSamples = Math.min(minSamples, values.size());
            }
            if (minSamples >= 2 && minSamples < Integer.MAX_VALUE) {
                for (int rowIndex = 0; rowIndex < minSamples; rowIndex++) {
                    double[] row = new double[algorithms.size()];
                    for (int i = 0; i < algorithms.size(); i++) {
                        row[i] = samples.get(algorithms.get(i)).get(rowIndex);
                    }
                    matrixRows.add(row);
                }
            }
        }

        if (matrixRows.size() < 2) {
            return new FriedmanTestResult(matrixRows.size(), algorithms.size(), null, null, List.of());
        }

        double[][] matrix = matrixRows.toArray(new double[0][]);
        StatisticsUtils.FriedmanComputation computed = StatisticsUtils.friedman(matrix, algorithms, minimize);
        return new FriedmanTestResult(
                matrixRows.size(),
                algorithms.size(),
                computed.statistic(),
                computed.pValue(),
                computed.ranks()
        );
    }

    private static Double computeErt(List<RunValueRow> rows, long successfulRuns) {
        if (successfulRuns <= 0L) {
            return null;
        }
        double totalEval = rows.stream()
                .filter(r -> r.evaluations() != null)
                .mapToDouble(r -> r.evaluations().doubleValue())
                .sum();
        return totalEval / successfulRuns;
    }

    private static Double computeErtFromOutcomes(List<RunOutcome> outcomes, long successfulRuns) {
        if (successfulRuns <= 0L) {
            return null;
        }
        double totalEval = outcomes.stream()
                .mapToDouble(outcome -> {
                    if (outcome.evaluationToTarget() != null) {
                        return outcome.evaluationToTarget().doubleValue();
                    }
                    return Math.max(0L, outcome.evaluationBudget());
                })
                .sum();
        return totalEval / successfulRuns;
    }

    private static Double computeSp1(List<RunValueRow> rows, Predicate<RunValueRow> success, double successRate) {
        if (successRate <= 0.0) {
            return null;
        }
        List<Double> successEvals = rows.stream()
                .filter(success)
                .filter(r -> r.evaluations() != null)
                .map(r -> r.evaluations().doubleValue())
                .toList();
        if (successEvals.isEmpty()) {
            return null;
        }
        Double meanSuccessEval = StatisticsUtils.mean(successEvals);
        if (meanSuccessEval == null) {
            return null;
        }
        return meanSuccessEval / successRate;
    }

    private static Double computeSp1FromOutcomes(List<RunOutcome> outcomes, double successRate) {
        if (successRate <= 0.0) {
            return null;
        }
        List<Double> successEvals = outcomes.stream()
                .map(RunOutcome::evaluationToTarget)
                .filter(value -> value != null && value > 0L)
                .map(value -> value.doubleValue())
                .toList();
        if (successEvals.isEmpty()) {
            return null;
        }
        Double meanSuccessEval = StatisticsUtils.mean(successEvals);
        if (meanSuccessEval == null) {
            return null;
        }
        return meanSuccessEval / successRate;
    }

    private static boolean isSuccessful(RunValueRow row, boolean minimize, Double targetFitness) {
        if (!"COMPLETED".equalsIgnoreCase(row.status())) {
            return false;
        }
        if (targetFitness == null) {
            return true;
        }
        if (row.bestFitness() == null) {
            return false;
        }
        return minimize ? row.bestFitness() <= targetFitness : row.bestFitness() >= targetFitness;
    }

    private static boolean isABetter(double a, double b, boolean minimize) {
        return minimize ? a < b : a > b;
    }

    private static boolean isMinimize(String direction, String problemType) {
        if (hasText(direction)) {
            return !"max".equalsIgnoreCase(direction.trim());
        }
        return problemType != null && MINIMIZE_PROBLEMS.contains(problemType.toLowerCase(Locale.ROOT));
    }

    private static String resolveDirectionLabel(String direction, String problemType) {
        return isMinimize(direction, problemType) ? "min" : "max";
    }

    private static List<Long> logarithmicBudgets(long min, long max, int points) {
        if (max <= min || points <= 1) {
            return List.of(max);
        }
        List<Long> values = new ArrayList<>(points + 1);
        double logMin = Math.log(min);
        double logMax = Math.log(max);
        for (int i = 0; i < points; i++) {
            double t = i / (double) (points - 1);
            long budget = Math.round(Math.exp(logMin * (1.0 - t) + logMax * t));
            values.add(Math.max(1L, budget));
        }
        values.add(max);
        return values.stream().distinct().sorted().toList();
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

    private static ExperimentQuery normalize(ExperimentQuery query) {
        ExperimentQuery base = query == null ? ExperimentQuery.defaults() : query;
        int safePage = Math.max(0, base.page());
        int safeSize = Math.max(1, Math.min(base.size(), 200));
        String sortBy = hasText(base.sortBy()) ? base.sortBy().toLowerCase(Locale.ROOT) : "latest_run_time";
        String sortDir = "asc".equalsIgnoreCase(base.sortDir()) ? "asc" : "desc";
        return new ExperimentQuery(
                trimToNull(base.q()),
                trimToNull(base.algorithm()),
                trimToNull(base.model()),
                trimToNull(base.problem()),
                trimToNull(base.status()),
                trimToNull(base.from()),
                trimToNull(base.to()),
                safePage,
                safeSize,
                sortBy,
                sortDir
        );
    }

    private static String deriveExperimentStatus(Long totalRuns,
                                                 Long completedRuns,
                                                 Long failedRuns,
                                                 Long runningRuns) {
        long total = totalRuns == null ? 0L : totalRuns;
        long completed = completedRuns == null ? 0L : completedRuns;
        long failed = failedRuns == null ? 0L : failedRuns;
        long running = runningRuns == null ? 0L : runningRuns;
        if (running > 0L) {
            return "RUNNING";
        }
        if (total > 0L && completed == total) {
            return "COMPLETED";
        }
        if (total > 0L && failed == total) {
            return "FAILED";
        }
        return "PARTIAL";
    }

    private static WhereClause buildExperimentFilters(ExperimentQuery query) {
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
            String status = query.status().toUpperCase(Locale.ROOT);
            switch (status) {
                case "RUNNING" -> sql.append("""
                        AND EXISTS (
                            SELECT 1
                            FROM runs rs
                            WHERE rs.experiment_id = e.experiment_id
                              AND LOWER(COALESCE(rs.status, '')) = 'running'
                        )
                        """);
                case "COMPLETED" -> sql.append("""
                        AND EXISTS (
                            SELECT 1 FROM runs rs
                            WHERE rs.experiment_id = e.experiment_id
                        )
                        AND NOT EXISTS (
                            SELECT 1
                            FROM runs rs
                            WHERE rs.experiment_id = e.experiment_id
                              AND LOWER(COALESCE(rs.status, '')) <> 'completed'
                        )
                        """);
                case "FAILED" -> sql.append("""
                        AND EXISTS (
                            SELECT 1 FROM runs rs
                            WHERE rs.experiment_id = e.experiment_id
                        )
                        AND NOT EXISTS (
                            SELECT 1
                            FROM runs rs
                            WHERE rs.experiment_id = e.experiment_id
                              AND LOWER(COALESCE(rs.status, '')) NOT IN ('failed', 'stopped')
                        )
                        """);
                case "PARTIAL" -> sql.append("""
                        AND EXISTS (
                            SELECT 1 FROM runs rs
                            WHERE rs.experiment_id = e.experiment_id
                        )
                        AND NOT EXISTS (
                            SELECT 1
                            FROM runs rs
                            WHERE rs.experiment_id = e.experiment_id
                              AND LOWER(COALESCE(rs.status, '')) = 'running'
                        )
                        AND EXISTS (
                            SELECT 1
                            FROM runs rs
                            WHERE rs.experiment_id = e.experiment_id
                              AND LOWER(COALESCE(rs.status, '')) <> 'completed'
                        )
                        AND EXISTS (
                            SELECT 1
                            FROM runs rs
                            WHERE rs.experiment_id = e.experiment_id
                              AND LOWER(COALESCE(rs.status, '')) NOT IN ('failed', 'stopped')
                        )
                        """);
                default -> {
                    // Ignore unsupported values and keep query behavior deterministic.
                }
            }
        }
        if (hasText(query.from())) {
            sql.append(" AND e.created_at >= ? ");
            params.add(query.from());
        }
        if (hasText(query.to())) {
            sql.append(" AND e.created_at <= ? ");
            params.add(query.to());
        }

        if (hasText(query.q())) {
            String like = like(query.q());
            sql.append("""
                     AND (
                        LOWER(e.experiment_id) LIKE ?
                        OR LOWER(COALESCE(e.run_name, '')) LIKE ?
                        OR LOWER(e.algorithm_type) LIKE ?
                        OR LOWER(e.model_type) LIKE ?
                        OR LOWER(e.problem_type) LIKE ?
                        OR LOWER(e.config_hash) LIKE ?
                        OR EXISTS (
                            SELECT 1
                            FROM runs rq
                            WHERE rq.experiment_id = e.experiment_id
                              AND LOWER(rq.run_id) LIKE ?
                        )
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
            params.add(like);
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

    private record RunValueRow(
            String runId,
            String algorithmType,
            String problemType,
            long seed,
            String status,
            Double bestFitness,
            Long evaluations,
            Long runtimeMillis
    ) {
    }

    private record IterationPoint(
            long evaluations,
            double bestFitness
    ) {
    }

    private record RunOutcome(
            String runId,
            String status,
            Double bestFitness,
            Long runtimeMillis,
            long evaluationBudget,
            Long evaluationToTarget,
            boolean successful
    ) {
    }

    private record ResolvedTarget(
            Double value,
            String source
    ) {
    }

    private record PairwiseDraft(
            String algorithmA,
            String algorithmB,
            long sampleSizeA,
            long sampleSizeB,
            double pValue,
            String betterAlgorithm
    ) {
    }
}
