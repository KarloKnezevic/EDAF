package com.knezevic.edaf.v3.persistence.query;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * JDBC read repository used by reporting and web dashboard.
 */
public final class JdbcRunRepository implements RunRepository {

    private static final Map<String, String> SORT_COLUMNS = new LinkedHashMap<>();
    private static final Map<String, String> EXPERIMENT_SORT_COLUMNS = new LinkedHashMap<>();
    private static final Map<String, String> EXPERIMENT_RUN_SORT_COLUMNS = new LinkedHashMap<>();

    static {
        SORT_COLUMNS.put("start_time", "r.start_time");
        SORT_COLUMNS.put("best_fitness", "r.best_fitness");
        SORT_COLUMNS.put("runtime_millis", "r.runtime_millis");
        SORT_COLUMNS.put("status", "r.status");

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
            "coco-bbob"
    );

    private final DataSource dataSource;

    public JdbcRunRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public PageResult<ExperimentListItem> listExperiments(ExperimentQuery query) {
        ExperimentQuery effective = normalize(query);
        WhereClause filters = buildExperimentFilters(effective);
        String sortColumn = EXPERIMENT_SORT_COLUMNS.getOrDefault(effective.sortBy(), "e.created_at");
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
                    MAX(r.start_time) AS latest_run_time,
                    COUNT(r.run_id) AS total_runs,
                    SUM(CASE WHEN LOWER(COALESCE(r.status, '')) = 'completed' THEN 1 ELSE 0 END) AS completed_runs,
                    SUM(CASE WHEN LOWER(COALESCE(r.status, '')) = 'failed' THEN 1 ELSE 0 END) AS failed_runs,
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
                                getNullableLong(rs, "total_runs"),
                                getNullableLong(rs, "completed_runs"),
                                getNullableLong(rs, "failed_runs"),
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
                    COALESCE(SUM(CASE WHEN r.status = 'FAILED' THEN 1 ELSE 0 END), 0) AS failed_runs,
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

    @Override
    public ExperimentAnalytics analyzeExperiment(String experimentId, String objectiveDirection, Double targetFitness) {
        ExperimentDetail detail = getExperimentDetail(experimentId);
        if (detail == null) {
            return new ExperimentAnalytics(
                    experimentId,
                    resolveDirectionLabel(objectiveDirection, null),
                    targetFitness,
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
                    List.of()
            );
        }

        List<RunValueRow> rows = loadRunValuesForExperiment(experimentId);
        boolean minimize = isMinimize(objectiveDirection, detail.problemType());
        Predicate<RunValueRow> success = row -> isSuccessful(row, minimize, targetFitness);

        long totalRuns = rows.size();
        long completedRuns = rows.stream().filter(r -> "COMPLETED".equalsIgnoreCase(r.status())).count();
        long successfulRuns = rows.stream().filter(success).count();
        double successRate = totalRuns == 0 ? 0.0 : successfulRuns / (double) totalRuns;

        List<Double> bestValues = rows.stream()
                .filter(r -> "COMPLETED".equalsIgnoreCase(r.status()) && r.bestFitness() != null)
                .map(RunValueRow::bestFitness)
                .toList();
        List<Double> runtimeValues = rows.stream()
                .filter(r -> r.runtimeMillis() != null)
                .map(r -> r.runtimeMillis().doubleValue())
                .toList();
        List<Double> evaluationValues = rows.stream()
                .filter(r -> r.evaluations() != null)
                .map(r -> r.evaluations().doubleValue())
                .toList();

        Double ert = computeErt(rows, successfulRuns);
        Double sp1 = computeSp1(rows, success, successRate);

        return new ExperimentAnalytics(
                experimentId,
                minimize ? "min" : "max",
                targetFitness,
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
                buildSingleDataProfile(rows, success),
                buildSinglePerformanceProfile(rows, success)
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
        String sortBy = hasText(base.sortBy()) ? base.sortBy().toLowerCase(Locale.ROOT) : "created_at";
        String sortDir = "asc".equalsIgnoreCase(base.sortDir()) ? "asc" : "desc";
        return new ExperimentQuery(
                trimToNull(base.q()),
                trimToNull(base.algorithm()),
                trimToNull(base.model()),
                trimToNull(base.problem()),
                trimToNull(base.from()),
                trimToNull(base.to()),
                safePage,
                safeSize,
                sortBy,
                sortDir
        );
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
