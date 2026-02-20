package com.knezevic.edaf.v3.coco.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knezevic.edaf.v3.coco.config.CocoCampaignConfig;
import com.knezevic.edaf.v3.coco.model.CocoAggregateRow;
import com.knezevic.edaf.v3.coco.model.CocoCampaignSnapshot;
import com.knezevic.edaf.v3.coco.model.CocoOptimizerRow;
import com.knezevic.edaf.v3.coco.model.CocoTrialOutcome;
import com.knezevic.edaf.v3.coco.model.CocoTrialRow;
import com.knezevic.edaf.v3.core.config.ExperimentConfig;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * JDBC write/read helper for COCO campaign data.
 */
public final class CocoJdbcStore {

    private static final double REFERENCE_TARGET_TOLERANCE_MIN = 1.0e-12;
    private static final double REFERENCE_TARGET_TOLERANCE_REL = 1.0e-6;

    private final DataSource dataSource;
    private final ObjectMapper mapper;

    public CocoJdbcStore(DataSource dataSource) {
        this.dataSource = dataSource;
        this.mapper = new ObjectMapper();
    }

    /**
     * Creates or updates campaign row and marks it running.
     */
    public void upsertCampaignStarted(CocoCampaignConfig config) {
        String sql = """
                INSERT INTO coco_campaigns (
                    campaign_id, name, suite, dimensions_json, instances_json, functions_json,
                    status, created_at, started_at, notes
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(campaign_id) DO UPDATE SET
                    name = excluded.name,
                    suite = excluded.suite,
                    dimensions_json = excluded.dimensions_json,
                    instances_json = excluded.instances_json,
                    functions_json = excluded.functions_json,
                    status = excluded.status,
                    started_at = excluded.started_at,
                    notes = excluded.notes
                """;
        CocoCampaignConfig.CampaignSection campaign = config.getCampaign();
        String now = Instant.now().toString();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, campaign.getId());
            statement.setString(2, campaign.getName());
            statement.setString(3, campaign.getSuite());
            statement.setString(4, mapper.writeValueAsString(campaign.getDimensions()));
            statement.setString(5, mapper.writeValueAsString(campaign.getInstances()));
            statement.setString(6, mapper.writeValueAsString(campaign.getFunctions()));
            statement.setString(7, "RUNNING");
            statement.setString(8, now);
            statement.setString(9, now);
            statement.setString(10, campaign.getNotes());
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed upserting COCO campaign start row", e);
        }
    }

    /**
     * Marks campaign final state.
     */
    public void updateCampaignStatus(String campaignId, String status, String notes) {
        String sql = """
                UPDATE coco_campaigns
                SET status = ?, finished_at = ?, notes = COALESCE(?, notes)
                WHERE campaign_id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setString(2, Instant.now().toString());
            statement.setString(3, notes);
            statement.setString(4, campaignId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed updating COCO campaign status", e);
        }
    }

    /**
     * Registers optimizer config used in campaign.
     */
    public void upsertOptimizer(String campaignId,
                                CocoCampaignConfig.OptimizerSection optimizer,
                                ExperimentConfig experimentConfig,
                                String configYaml) {
        String sql = """
                INSERT INTO coco_optimizer_configs (
                    campaign_id, optimizer_id, config_path, algorithm_type,
                    model_type, representation_type, config_yaml, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(campaign_id, optimizer_id) DO UPDATE SET
                    config_path = excluded.config_path,
                    algorithm_type = excluded.algorithm_type,
                    model_type = excluded.model_type,
                    representation_type = excluded.representation_type,
                    config_yaml = excluded.config_yaml
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, campaignId);
            statement.setString(2, optimizer.getId());
            statement.setString(3, optimizer.getConfig());
            statement.setString(4, experimentConfig.getAlgorithm().getType());
            statement.setString(5, experimentConfig.getModel().getType());
            statement.setString(6, experimentConfig.getRepresentation().getType());
            statement.setString(7, configYaml);
            statement.setString(8, Instant.now().toString());
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed upserting COCO optimizer row", e);
        }
    }

    /**
     * Persists one trial result.
     */
    public void upsertTrial(CocoTrialOutcome trial) {
        String sql = """
                INSERT INTO coco_trials (
                    campaign_id, optimizer_id, run_id,
                    function_id, instance_id, dimension, repetition,
                    budget_evals, evaluations, best_fitness, runtime_millis,
                    status, reached_target, evals_to_target, target_value, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(campaign_id, optimizer_id, function_id, instance_id, dimension, repetition) DO UPDATE SET
                    run_id = excluded.run_id,
                    budget_evals = excluded.budget_evals,
                    evaluations = excluded.evaluations,
                    best_fitness = excluded.best_fitness,
                    runtime_millis = excluded.runtime_millis,
                    status = excluded.status,
                    reached_target = excluded.reached_target,
                    evals_to_target = excluded.evals_to_target,
                    target_value = excluded.target_value,
                    created_at = excluded.created_at
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trial.campaignId());
            statement.setString(2, trial.optimizerId());
            statement.setString(3, trial.runId());
            statement.setInt(4, trial.functionId());
            statement.setInt(5, trial.instanceId());
            statement.setInt(6, trial.dimension());
            statement.setInt(7, trial.repetition());
            statement.setLong(8, trial.budgetEvaluations());
            setNullableLong(statement, 9, trial.evaluations());
            setNullableDouble(statement, 10, trial.bestFitness());
            setNullableLong(statement, 11, trial.runtimeMillis());
            statement.setString(12, trial.status());
            statement.setInt(13, trial.reachedTarget() ? 1 : 0);
            setNullableLong(statement, 14, trial.evaluationsToTarget());
            statement.setDouble(15, trial.targetValue());
            statement.setString(16, Instant.now().toString());
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed upserting COCO trial row", e);
        }
    }

    /**
     * Finds evaluations count where best fitness first reached target, or null if never reached.
     */
    public Long findEvaluationsToTarget(String runId, double target) {
        String sql = """
                SELECT evaluations
                FROM iterations
                WHERE run_id = ? AND best_fitness <= ?
                ORDER BY iteration ASC
                LIMIT 1
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, runId);
            statement.setDouble(2, target);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    long value = rs.getLong(1);
                    return rs.wasNull() ? null : value;
                }
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed querying evaluations-to-target for run " + runId, e);
        }
    }

    /**
     * Rebuilds campaign aggregates from trial rows and optional imported references.
     */
    public void rebuildAggregates(String campaignId, String suite, double targetValue, String referenceMode) {
        List<CocoTrialRow> trials = listTrials(campaignId);
        deleteAggregates(campaignId);

        Map<OptimizerDimensionKey, List<CocoTrialRow>> grouped = trials.stream()
                .collect(Collectors.groupingBy(t -> new OptimizerDimensionKey(t.optimizerId(), t.dimension()),
                        LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<OptimizerDimensionKey, List<CocoTrialRow>> entry : grouped.entrySet()) {
            OptimizerDimensionKey key = entry.getKey();
            List<CocoTrialRow> rows = entry.getValue();

            int total = rows.size();
            int successCount = (int) rows.stream().filter(CocoTrialRow::reachedTarget).count();
            double successRate = total == 0 ? 0.0 : successCount / (double) total;

            Double meanEvaluationsToTarget = successCount == 0
                    ? null
                    : rows.stream()
                    .filter(CocoTrialRow::reachedTarget)
                    .map(CocoTrialRow::evaluationsToTarget)
                    .filter(Objects::nonNull)
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(Double.NaN);
            if (meanEvaluationsToTarget != null && meanEvaluationsToTarget.isNaN()) {
                meanEvaluationsToTarget = null;
            }

            List<Double> bestFitnessValues = rows.stream()
                    .map(CocoTrialRow::bestFitness)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.naturalOrder())
                    .toList();
            Double medianBest = median(bestFitnessValues);

            Double edafErt = computeErt(rows, successCount);
            ReferenceSnapshot reference = lookupReference(suite, key.dimension(), targetValue, rows, referenceMode);
            Double ratio = edafErt != null && reference.referenceErt() != null && reference.referenceErt() > 0
                    ? edafErt / reference.referenceErt()
                    : null;

            insertAggregate(new CocoAggregateRow(
                    campaignId,
                    key.optimizerId(),
                    key.dimension(),
                    targetValue,
                    meanEvaluationsToTarget,
                    successRate,
                    medianBest,
                    reference.referenceLabel(),
                    reference.referenceErt(),
                    edafErt,
                    ratio
            ));
        }
    }

    /**
     * Imports reference data CSV into baseline table.
     */
    public int importReferenceCsv(Path csvPath, String suite, String sourceUrl) {
        List<String> lines;
        try {
            lines = Files.readAllLines(csvPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed reading reference CSV: " + csvPath, e);
        }
        if (lines.isEmpty()) {
            return 0;
        }

        int imported = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (i == 0 && line.toLowerCase(Locale.ROOT).contains("optimizer_name")) {
                continue;
            }

            String[] tokens = line.split(",");
            if (tokens.length < 6) {
                throw new IllegalArgumentException("Invalid reference CSV row (expected >= 6 columns): " + line);
            }

            String optimizerName = tokens[0].trim();
            int functionId = Integer.parseInt(tokens[1].trim());
            int dimension = Integer.parseInt(tokens[2].trim());
            double targetValue = Double.parseDouble(tokens[3].trim());
            double ert = Double.parseDouble(tokens[4].trim());
            Double successRate = tokens[5].trim().isEmpty() ? null : Double.parseDouble(tokens[5].trim());

            insertReferenceRow(suite, optimizerName, functionId, dimension, targetValue, ert, successRate, sourceUrl);
            imported++;
        }
        return imported;
    }

    /**
     * Loads a complete snapshot used by COCO HTML report generation.
     */
    public CocoCampaignSnapshot loadSnapshot(String campaignId) {
        String campaignSql = """
                SELECT campaign_id, name, suite, status, created_at, started_at, finished_at,
                       dimensions_json, instances_json, functions_json, notes
                FROM coco_campaigns
                WHERE campaign_id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(campaignSql)) {
            statement.setString(1, campaignId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new CocoCampaignSnapshot(
                        rs.getString("campaign_id"),
                        rs.getString("name"),
                        rs.getString("suite"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("started_at"),
                        rs.getString("finished_at"),
                        rs.getString("dimensions_json"),
                        rs.getString("instances_json"),
                        rs.getString("functions_json"),
                        rs.getString("notes"),
                        listOptimizers(campaignId),
                        listAggregates(campaignId),
                        listTrials(campaignId)
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed loading COCO campaign snapshot for " + campaignId, e);
        }
    }

    public List<CocoTrialRow> listTrials(String campaignId) {
        String sql = """
                SELECT campaign_id, optimizer_id, run_id, function_id, instance_id, dimension,
                       repetition, budget_evals, evaluations, best_fitness, runtime_millis,
                       status, reached_target, evals_to_target, target_value, created_at
                FROM coco_trials
                WHERE campaign_id = ?
                ORDER BY optimizer_id, dimension, function_id, instance_id, repetition
                """;
        List<CocoTrialRow> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, campaignId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new CocoTrialRow(
                            rs.getString("campaign_id"),
                            rs.getString("optimizer_id"),
                            rs.getString("run_id"),
                            rs.getInt("function_id"),
                            rs.getInt("instance_id"),
                            rs.getInt("dimension"),
                            rs.getInt("repetition"),
                            rs.getLong("budget_evals"),
                            nullableLong(rs, "evaluations"),
                            nullableDouble(rs, "best_fitness"),
                            nullableLong(rs, "runtime_millis"),
                            rs.getString("status"),
                            rs.getInt("reached_target") == 1,
                            nullableLong(rs, "evals_to_target"),
                            rs.getDouble("target_value"),
                            rs.getString("created_at")
                    ));
                }
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed listing COCO trials for campaign " + campaignId, e);
        }
    }

    public List<CocoAggregateRow> listAggregates(String campaignId) {
        String sql = """
                SELECT campaign_id, optimizer_id, dimension, target_value,
                       mean_evals_to_target, success_rate, median_best_fitness,
                       compared_reference_optimizer, reference_ert, edaf_ert, ert_ratio
                FROM coco_aggregates
                WHERE campaign_id = ?
                ORDER BY optimizer_id, dimension
                """;
        List<CocoAggregateRow> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, campaignId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new CocoAggregateRow(
                            rs.getString("campaign_id"),
                            rs.getString("optimizer_id"),
                            rs.getInt("dimension"),
                            rs.getDouble("target_value"),
                            nullableDouble(rs, "mean_evals_to_target"),
                            rs.getDouble("success_rate"),
                            nullableDouble(rs, "median_best_fitness"),
                            rs.getString("compared_reference_optimizer"),
                            nullableDouble(rs, "reference_ert"),
                            nullableDouble(rs, "edaf_ert"),
                            nullableDouble(rs, "ert_ratio")
                    ));
                }
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed listing COCO aggregates for campaign " + campaignId, e);
        }
    }

    public List<CocoOptimizerRow> listOptimizers(String campaignId) {
        String sql = """
                SELECT campaign_id, optimizer_id, config_path, algorithm_type, model_type, representation_type, created_at
                FROM coco_optimizer_configs
                WHERE campaign_id = ?
                ORDER BY optimizer_id
                """;
        List<CocoOptimizerRow> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, campaignId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new CocoOptimizerRow(
                            rs.getString("campaign_id"),
                            rs.getString("optimizer_id"),
                            rs.getString("config_path"),
                            rs.getString("algorithm_type"),
                            rs.getString("model_type"),
                            rs.getString("representation_type"),
                            rs.getString("created_at")
                    ));
                }
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed listing COCO optimizers for campaign " + campaignId, e);
        }
    }

    private void deleteAggregates(String campaignId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM coco_aggregates WHERE campaign_id = ?")) {
            statement.setString(1, campaignId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed deleting COCO aggregates for campaign " + campaignId, e);
        }
    }

    private void insertAggregate(CocoAggregateRow aggregate) {
        String sql = """
                INSERT INTO coco_aggregates (
                    campaign_id, optimizer_id, dimension, target_value,
                    mean_evals_to_target, success_rate, median_best_fitness,
                    compared_reference_optimizer, reference_ert, edaf_ert, ert_ratio, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, aggregate.campaignId());
            statement.setString(2, aggregate.optimizerId());
            statement.setInt(3, aggregate.dimension());
            statement.setDouble(4, aggregate.targetValue());
            setNullableDouble(statement, 5, aggregate.meanEvaluationsToTarget());
            statement.setDouble(6, aggregate.successRate());
            setNullableDouble(statement, 7, aggregate.medianBestFitness());
            statement.setString(8, aggregate.comparedReferenceOptimizer());
            setNullableDouble(statement, 9, aggregate.referenceErt());
            setNullableDouble(statement, 10, aggregate.edafErt());
            setNullableDouble(statement, 11, aggregate.ertRatio());
            statement.setString(12, Instant.now().toString());
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed inserting COCO aggregate row", e);
        }
    }

    private void insertReferenceRow(String suite,
                                    String optimizerName,
                                    int functionId,
                                    int dimension,
                                    double targetValue,
                                    double ert,
                                    Double successRate,
                                    String sourceUrl) {
        String sql = """
                INSERT INTO coco_reference_results (
                    suite, optimizer_name, function_id, dimension, target_value,
                    ert, success_rate, source_url, imported_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, suite);
            statement.setString(2, optimizerName);
            statement.setInt(3, functionId);
            statement.setInt(4, dimension);
            statement.setDouble(5, targetValue);
            statement.setDouble(6, ert);
            setNullableDouble(statement, 7, successRate);
            statement.setString(8, sourceUrl);
            statement.setString(9, Instant.now().toString());
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed inserting COCO reference row", e);
        }
    }

    private ReferenceSnapshot lookupReference(String suite,
                                              int dimension,
                                              double targetValue,
                                              List<CocoTrialRow> trials,
                                              String referenceMode) {
        List<Integer> functionIds = trials.stream().map(CocoTrialRow::functionId).distinct().sorted().toList();
        if (functionIds.isEmpty()) {
            return new ReferenceSnapshot(referenceMode, null);
        }

        String mode = referenceMode == null ? "best-online" : referenceMode.trim();
        if (mode.toLowerCase(Locale.ROOT).startsWith("optimizer:")) {
            String optimizerName = mode.substring("optimizer:".length()).trim();
            return new ReferenceSnapshot(optimizerName,
                    queryReferenceAverageForOptimizer(suite, dimension, targetValue, functionIds, optimizerName));
        }
        return new ReferenceSnapshot("best-online",
                queryReferenceAverageBestOnline(suite, dimension, targetValue, functionIds));
    }

    private Double queryReferenceAverageForOptimizer(String suite,
                                                     int dimension,
                                                     double targetValue,
                                                     List<Integer> functionIds,
                                                     String optimizerName) {
        String in = functionIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = """
                SELECT AVG(ert)
                FROM coco_reference_results
                WHERE suite = ?
                  AND dimension = ?
                  AND optimizer_name = ?
                  AND ABS(target_value - ?) <= ?
                  AND function_id IN (
                """ + in + ")";

        double tolerance = Math.max(REFERENCE_TARGET_TOLERANCE_MIN, Math.abs(targetValue) * REFERENCE_TARGET_TOLERANCE_REL);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int idx = 1;
            statement.setString(idx++, suite);
            statement.setInt(idx++, dimension);
            statement.setString(idx++, optimizerName);
            statement.setDouble(idx++, targetValue);
            statement.setDouble(idx++, tolerance);
            for (Integer functionId : functionIds) {
                statement.setInt(idx++, functionId);
            }
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return nullableDouble(rs, 1);
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed querying COCO reference averages", e);
        }
    }

    private Double queryReferenceAverageBestOnline(String suite,
                                                   int dimension,
                                                   double targetValue,
                                                   List<Integer> functionIds) {
        String in = functionIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = """
                SELECT AVG(best_ert)
                FROM (
                    SELECT function_id, MIN(ert) AS best_ert
                    FROM coco_reference_results
                    WHERE suite = ?
                      AND dimension = ?
                      AND ABS(target_value - ?) <= ?
                      AND function_id IN (
                """ + in + ") GROUP BY function_id) t";

        double tolerance = Math.max(REFERENCE_TARGET_TOLERANCE_MIN, Math.abs(targetValue) * REFERENCE_TARGET_TOLERANCE_REL);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int idx = 1;
            statement.setString(idx++, suite);
            statement.setInt(idx++, dimension);
            statement.setDouble(idx++, targetValue);
            statement.setDouble(idx++, tolerance);
            for (Integer functionId : functionIds) {
                statement.setInt(idx++, functionId);
            }
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return nullableDouble(rs, 1);
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed querying best-online reference averages", e);
        }
    }

    private static Double computeErt(List<CocoTrialRow> rows, int successCount) {
        if (successCount == 0) {
            return null;
        }
        double sum = 0.0;
        for (CocoTrialRow row : rows) {
            if (row.reachedTarget() && row.evaluationsToTarget() != null) {
                sum += row.evaluationsToTarget();
            } else {
                sum += row.budgetEvaluations();
            }
        }
        return sum / successCount;
    }

    private static Double median(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        int n = values.size();
        if ((n & 1) == 1) {
            return values.get(n / 2);
        }
        return (values.get(n / 2 - 1) + values.get(n / 2)) / 2.0;
    }

    private static void setNullableLong(PreparedStatement statement, int index, Long value) throws Exception {
        if (value == null) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setLong(index, value);
        }
    }

    private static void setNullableDouble(PreparedStatement statement, int index, Double value) throws Exception {
        if (value == null) {
            statement.setNull(index, Types.DOUBLE);
        } else {
            statement.setDouble(index, value);
        }
    }

    private static Long nullableLong(ResultSet rs, String column) throws Exception {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Double nullableDouble(ResultSet rs, String column) throws Exception {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private static Double nullableDouble(ResultSet rs, int column) throws Exception {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private record OptimizerDimensionKey(String optimizerId, int dimension) {
    }

    private record ReferenceSnapshot(String referenceLabel, Double referenceErt) {
    }
}
