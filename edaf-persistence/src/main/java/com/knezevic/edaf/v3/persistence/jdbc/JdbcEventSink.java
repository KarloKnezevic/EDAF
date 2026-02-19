package com.knezevic.edaf.v3.persistence.jdbc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.knezevic.edaf.v3.core.config.ExperimentConfig;
import com.knezevic.edaf.v3.core.events.CheckpointSavedEvent;
import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.core.events.IterationCompletedEvent;
import com.knezevic.edaf.v3.core.events.RunCompletedEvent;
import com.knezevic.edaf.v3.core.events.RunEvent;
import com.knezevic.edaf.v3.core.events.RunFailedEvent;
import com.knezevic.edaf.v3.core.events.RunResumedEvent;
import com.knezevic.edaf.v3.core.events.RunStartedEvent;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDBC sink persisting experiment metadata, runs, objectives, iterations, checkpoints, and raw events.
 */
public final class JdbcEventSink implements EventSink {

    private final DataSource dataSource;
    private final ExperimentConfig config;
    private final String canonicalYaml;
    private final String canonicalJson;
    private final String configHash;
    private final String experimentId;
    private final String experimentCreatedAt;
    private final ObjectMapper eventMapper;
    private final ObjectMapper canonicalMapper;
    private final JsonNode canonicalConfigNode;
    private final Map<String, Map<String, Double>> latestMetricsByRun = new ConcurrentHashMap<>();

    private volatile boolean experimentInitialized;

    public JdbcEventSink(DataSource dataSource, ExperimentConfig config, String canonicalYaml, String canonicalJson) {
        this.dataSource = dataSource;
        this.config = config;
        this.canonicalYaml = canonicalYaml;
        this.canonicalJson = canonicalJson;
        this.configHash = sha256(canonicalJson);
        this.experimentId = this.configHash;
        this.experimentCreatedAt = Instant.now().toString();
        this.eventMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.canonicalMapper = new ObjectMapper()
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        try {
            this.canonicalConfigNode = canonicalMapper.readTree(canonicalJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid canonical JSON configuration", e);
        }
    }

    @Override
    public void onEvent(RunEvent event) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ensureExperimentMetadata(connection);
                if (event.runId() != null && !event.runId().isBlank()) {
                    ensureRunSkeleton(connection, event.runId(), event.timestamp().toString());
                }

                insertRawEvent(connection, event);

                if (event instanceof RunStartedEvent started) {
                    upsertRunStarted(connection, started);
                } else if (event instanceof RunResumedEvent resumed) {
                    upsertRunResumed(connection, resumed);
                } else if (event instanceof IterationCompletedEvent iteration) {
                    upsertIteration(connection, iteration);
                } else if (event instanceof RunCompletedEvent completed) {
                    upsertRunCompleted(connection, completed);
                } else if (event instanceof RunFailedEvent failed) {
                    upsertRunFailed(connection, failed);
                } else if (event instanceof CheckpointSavedEvent checkpoint) {
                    insertCheckpoint(connection, checkpoint);
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed writing JDBC event sink", e);
        }
    }

    private void ensureExperimentMetadata(Connection connection) throws SQLException {
        if (!experimentInitialized) {
            upsertExperiment(connection);
            rewriteExperimentParams(connection);
            experimentInitialized = true;
        }
    }

    private void upsertExperiment(Connection connection) throws SQLException {
        String sql = """
                INSERT INTO experiments(
                    experiment_id, config_hash, schema_version, run_name, algorithm_type, model_type,
                    problem_type, representation_type, selection_type, replacement_type, stopping_type,
                    max_iterations, config_yaml, config_json, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(config_hash) DO UPDATE SET
                    run_name = excluded.run_name,
                    schema_version = excluded.schema_version,
                    algorithm_type = excluded.algorithm_type,
                    model_type = excluded.model_type,
                    problem_type = excluded.problem_type,
                    representation_type = excluded.representation_type,
                    selection_type = excluded.selection_type,
                    replacement_type = excluded.replacement_type,
                    stopping_type = excluded.stopping_type,
                    max_iterations = excluded.max_iterations,
                    config_yaml = excluded.config_yaml,
                    config_json = excluded.config_json
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, experimentId);
            statement.setString(2, configHash);
            statement.setString(3, config.getSchema());
            statement.setString(4, config.getRun().getName());
            statement.setString(5, config.getAlgorithm().getType());
            statement.setString(6, config.getModel().getType());
            statement.setString(7, config.getProblem().getType());
            statement.setString(8, config.getRepresentation().getType());
            statement.setString(9, config.getSelection().getType());
            statement.setString(10, config.getReplacement().getType());
            statement.setString(11, config.getStopping().getType());
            statement.setInt(12, config.getStopping().getMaxIterations());
            statement.setString(13, canonicalYaml);
            statement.setString(14, canonicalJson);
            statement.setString(15, experimentCreatedAt);
            statement.executeUpdate();
        }
    }

    private void rewriteExperimentParams(Connection connection) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM experiment_params WHERE experiment_id = ?")) {
            delete.setString(1, experimentId);
            delete.executeUpdate();
        }

        String insertSql = """
                INSERT INTO experiment_params(
                    experiment_id, section, param_path, leaf_key, value_type,
                    value_text, value_number, value_boolean, value_json
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(experiment_id, param_path) DO UPDATE SET
                    section = excluded.section,
                    leaf_key = excluded.leaf_key,
                    value_type = excluded.value_type,
                    value_text = excluded.value_text,
                    value_number = excluded.value_number,
                    value_boolean = excluded.value_boolean,
                    value_json = excluded.value_json
                """;

        List<FlattenedParam> rows = flattenConfigParams();
        try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
            for (FlattenedParam row : rows) {
                insert.setString(1, experimentId);
                insert.setString(2, row.section());
                insert.setString(3, row.paramPath());
                insert.setString(4, row.leafKey());
                insert.setString(5, row.valueType());
                insert.setString(6, row.valueText());
                if (row.valueNumber() == null) {
                    insert.setNull(7, Types.DOUBLE);
                } else {
                    insert.setDouble(7, row.valueNumber());
                }
                if (row.valueBoolean() == null) {
                    insert.setNull(8, Types.INTEGER);
                } else {
                    insert.setInt(8, row.valueBoolean());
                }
                insert.setString(9, row.valueJson());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private List<FlattenedParam> flattenConfigParams() {
        List<FlattenedParam> rows = new ArrayList<>();
        List<String> sections = new ArrayList<>();
        canonicalConfigNode.fieldNames().forEachRemaining(sections::add);
        sections.sort(String::compareTo);
        for (String section : sections) {
            flattenNode(section, section, canonicalConfigNode.path(section), rows);
        }
        rows.sort(Comparator.comparing(FlattenedParam::section).thenComparing(FlattenedParam::paramPath));
        return rows;
    }

    private void flattenNode(String section, String path, JsonNode node, List<FlattenedParam> rows) {
        String leafKey = extractLeafKey(path);
        if (node == null || node.isMissingNode() || node.isNull()) {
            rows.add(new FlattenedParam(section, path, leafKey, "null", null, null, null, null));
            return;
        }

        if (node.isObject()) {
            rows.add(new FlattenedParam(section, path, leafKey, "json", null, null, null, node.toString()));
            List<String> fields = new ArrayList<>();
            node.fieldNames().forEachRemaining(fields::add);
            fields.sort(String::compareTo);
            for (String field : fields) {
                JsonNode child = node.get(field);
                if ("params".equals(field) && isTypedSectionPath(path) && child != null && child.isObject()) {
                    List<String> paramFields = new ArrayList<>();
                    child.fieldNames().forEachRemaining(paramFields::add);
                    paramFields.sort(String::compareTo);
                    for (String paramField : paramFields) {
                        flattenNode(section, path + "." + paramField, child.get(paramField), rows);
                    }
                } else {
                    flattenNode(section, path + "." + field, child, rows);
                }
            }
            return;
        }

        if (node.isArray()) {
            rows.add(new FlattenedParam(section, path, leafKey, "json", null, null, null, node.toString()));
            for (int i = 0; i < node.size(); i++) {
                flattenNode(section, path + "[" + i + "]", node.get(i), rows);
            }
            return;
        }

        if (node.isTextual()) {
            rows.add(new FlattenedParam(section, path, leafKey, "string", node.textValue(), null, null, null));
            return;
        }

        if (node.isNumber()) {
            rows.add(new FlattenedParam(
                    section,
                    path,
                    leafKey,
                    "number",
                    node.numberValue().toString(),
                    node.doubleValue(),
                    null,
                    null
            ));
            return;
        }

        if (node.isBoolean()) {
            rows.add(new FlattenedParam(
                    section,
                    path,
                    leafKey,
                    "boolean",
                    Boolean.toString(node.booleanValue()),
                    null,
                    node.booleanValue() ? 1 : 0,
                    null
            ));
            return;
        }

        rows.add(new FlattenedParam(section, path, leafKey, "json", null, null, null, node.toString()));
    }

    private void ensureRunSkeleton(Connection connection, String runId, String timestamp) throws SQLException {
        String sql = """
                INSERT INTO runs(run_id, experiment_id, seed, status, start_time)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(run_id) DO NOTHING
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, runId);
            statement.setString(2, experimentId);
            statement.setLong(3, config.getRun().getMasterSeed());
            statement.setString(4, "RUNNING");
            statement.setString(5, timestamp);
            statement.executeUpdate();
        }
    }

    private void upsertRunStarted(Connection connection, RunStartedEvent event) throws SQLException {
        String sql = """
                UPDATE runs
                SET experiment_id = ?, seed = ?, status = ?, start_time = COALESCE(start_time, ?),
                    end_time = NULL, error_message = NULL
                WHERE run_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, experimentId);
            statement.setLong(2, event.masterSeed());
            statement.setString(3, "RUNNING");
            statement.setString(4, event.timestamp().toString());
            statement.setString(5, event.runId());
            statement.executeUpdate();
        }
    }

    private void upsertRunResumed(Connection connection, RunResumedEvent event) throws SQLException {
        String sql = """
                UPDATE runs
                SET status = ?, resumed_from = ?, error_message = NULL
                WHERE run_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "RUNNING");
            statement.setString(2, event.checkpointPath());
            statement.setString(3, event.runId());
            statement.executeUpdate();
        }
    }

    private void upsertIteration(Connection connection, IterationCompletedEvent event) throws SQLException {
        String sql = """
                INSERT INTO iterations(
                    run_id, iteration, evaluations, best_fitness, mean_fitness, std_fitness,
                    metrics_json, diagnostics_json, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(run_id, iteration) DO UPDATE SET
                    evaluations = excluded.evaluations,
                    best_fitness = excluded.best_fitness,
                    mean_fitness = excluded.mean_fitness,
                    std_fitness = excluded.std_fitness,
                    metrics_json = excluded.metrics_json,
                    diagnostics_json = excluded.diagnostics_json,
                    created_at = excluded.created_at
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, event.runId());
            statement.setInt(2, event.iteration());
            statement.setLong(3, event.evaluations());
            statement.setDouble(4, event.bestFitness());
            statement.setDouble(5, event.meanFitness());
            statement.setDouble(6, event.stdFitness());
            statement.setString(7, eventMapper.writeValueAsString(event.metrics()));
            statement.setString(8, eventMapper.writeValueAsString(event.diagnostics().numeric()));
            statement.setString(9, event.timestamp().toString());
            statement.executeUpdate();
        } catch (Exception e) {
            throw new SQLException("Failed serializing iteration payload", e);
        }

        latestMetricsByRun.put(event.runId(), new LinkedHashMap<>(event.metrics()));
    }

    private void upsertRunCompleted(Connection connection, RunCompletedEvent event) throws SQLException {
        String sql = """
                UPDATE runs
                SET status = ?, end_time = ?, iterations = ?, evaluations = ?, best_fitness = ?,
                    best_summary = ?, runtime_millis = ?, artifacts_json = ?, error_message = NULL
                WHERE run_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "COMPLETED");
            statement.setString(2, event.timestamp().toString());
            statement.setInt(3, event.iterations());
            statement.setLong(4, event.evaluations());
            statement.setDouble(5, event.bestFitness());
            statement.setString(6, event.bestSummary());
            statement.setLong(7, event.runtimeMillis());
            statement.setString(8, eventMapper.writeValueAsString(event.artifacts()));
            statement.setString(9, event.runId());
            statement.executeUpdate();
        } catch (Exception e) {
            throw new SQLException("Failed serializing run completion payload", e);
        }

        Map<String, Double> objectiveValues = latestMetricsByRun.remove(event.runId());
        upsertRunObjectives(connection, event.runId(), objectiveValues);
    }

    private void upsertRunFailed(Connection connection, RunFailedEvent event) throws SQLException {
        String sql = """
                INSERT INTO runs(
                    run_id, experiment_id, seed, status, start_time, end_time, resumed_from, error_message
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(run_id) DO UPDATE SET
                    status = excluded.status,
                    end_time = excluded.end_time,
                    resumed_from = COALESCE(excluded.resumed_from, runs.resumed_from),
                    error_message = excluded.error_message
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, event.runId());
            statement.setString(2, experimentId);
            statement.setLong(3, event.masterSeed());
            statement.setString(4, "FAILED");
            statement.setString(5, event.timestamp().toString());
            statement.setString(6, event.timestamp().toString());
            statement.setString(7, event.resumedFrom());
            statement.setString(8, event.errorMessage());
            statement.executeUpdate();
        }
        latestMetricsByRun.remove(event.runId());
    }

    private void upsertRunObjectives(Connection connection, String runId, Map<String, Double> objectiveValues) throws SQLException {
        if (objectiveValues == null || objectiveValues.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO run_objectives(run_id, objective_name, objective_value)
                VALUES (?, ?, ?)
                ON CONFLICT(run_id, objective_name) DO UPDATE SET
                    objective_value = excluded.objective_value
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<String, Double> entry : objectiveValues.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                statement.setString(1, runId);
                statement.setString(2, entry.getKey());
                statement.setDouble(3, entry.getValue());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertCheckpoint(Connection connection, CheckpointSavedEvent event) throws SQLException {
        String sql = """
                INSERT INTO checkpoints(run_id, iteration, checkpoint_path, created_at)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, event.runId());
            statement.setInt(2, event.iteration());
            statement.setString(3, event.checkpointPath());
            statement.setString(4, event.timestamp().toString());
            statement.executeUpdate();
        }
    }

    private void insertRawEvent(Connection connection, RunEvent event) throws SQLException {
        String sql = """
                INSERT INTO events(run_id, event_type, payload_json, created_at)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, event.runId());
            statement.setString(2, event.type());
            statement.setString(3, eventMapper.writeValueAsString(event));
            statement.setString(4, event.timestamp().toString());
            statement.executeUpdate();
        } catch (Exception e) {
            throw new SQLException("Failed serializing event payload", e);
        }
    }

    private static String extractLeafKey(String path) {
        int lastDot = path.lastIndexOf('.');
        int lastBracket = path.lastIndexOf('[');
        if (lastBracket > lastDot) {
            int endBracket = path.indexOf(']', lastBracket);
            if (endBracket > lastBracket) {
                return path.substring(lastBracket + 1, endBracket);
            }
        }
        if (lastDot >= 0 && lastDot + 1 < path.length()) {
            return path.substring(lastDot + 1);
        }
        return path;
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                out.append(String.format(Locale.ROOT, "%02x", b));
            }
            return out.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed computing config hash", e);
        }
    }

    private static boolean isTypedSectionPath(String path) {
        return "representation".equals(path)
                || "problem".equals(path)
                || "algorithm".equals(path)
                || "model".equals(path)
                || "selection".equals(path)
                || "replacement".equals(path)
                || "constraints".equals(path)
                || "localSearch".equals(path)
                || "restart".equals(path)
                || "niching".equals(path);
    }

    private record FlattenedParam(
            String section,
            String paramPath,
            String leafKey,
            String valueType,
            String valueText,
            Double valueNumber,
            Integer valueBoolean,
            String valueJson
    ) {
    }
}
