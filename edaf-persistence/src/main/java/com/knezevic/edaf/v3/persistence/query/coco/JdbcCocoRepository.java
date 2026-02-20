package com.knezevic.edaf.v3.persistence.query.coco;

import com.knezevic.edaf.v3.persistence.query.PageResult;

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
 * JDBC implementation of COCO read repository.
 */
public final class JdbcCocoRepository implements CocoRepository {

    private static final Map<String, String> CAMPAIGN_SORT_COLUMNS = new LinkedHashMap<>();

    static {
        CAMPAIGN_SORT_COLUMNS.put("created_at", "c.created_at");
        CAMPAIGN_SORT_COLUMNS.put("started_at", "c.started_at");
        CAMPAIGN_SORT_COLUMNS.put("finished_at", "c.finished_at");
        CAMPAIGN_SORT_COLUMNS.put("status", "c.status");
        CAMPAIGN_SORT_COLUMNS.put("name", "c.name");
    }

    private final DataSource dataSource;

    public JdbcCocoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public PageResult<CocoCampaignListItem> listCampaigns(CocoCampaignQuery query) {
        CocoCampaignQuery effective = normalize(query);
        WhereClause filters = buildCampaignFilters(effective);

        String sortColumn = CAMPAIGN_SORT_COLUMNS.getOrDefault(effective.sortBy(), CAMPAIGN_SORT_COLUMNS.get("created_at"));
        String sortDir = "asc".equalsIgnoreCase(effective.sortDir()) ? "ASC" : "DESC";

        String fromSql = """
                FROM coco_campaigns c
                WHERE 1 = 1
                """ + filters.sql();

        String countSql = "SELECT COUNT(*) " + fromSql;
        String dataSql = """
                SELECT
                    c.campaign_id,
                    c.name,
                    c.suite,
                    c.status,
                    c.created_at,
                    c.started_at,
                    c.finished_at,
                    (SELECT COUNT(*) FROM coco_trials t WHERE t.campaign_id = c.campaign_id) AS trials,
                    (SELECT COUNT(*) FROM coco_trials t WHERE t.campaign_id = c.campaign_id AND t.reached_target = 1) AS reached_targets,
                    (SELECT COUNT(*) FROM coco_optimizer_configs o WHERE o.campaign_id = c.campaign_id) AS optimizer_count
                """ + fromSql + " ORDER BY " + sortColumn + " " + sortDir + " LIMIT ? OFFSET ?";

        try (Connection connection = dataSource.getConnection()) {
            long total = queryCount(connection, countSql, filters.params());

            List<Object> dataParams = new ArrayList<>(filters.params());
            dataParams.add(effective.size());
            dataParams.add(effective.offset());

            List<CocoCampaignListItem> items = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(dataSql)) {
                bindParams(statement, dataParams);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        items.add(new CocoCampaignListItem(
                                rs.getString("campaign_id"),
                                rs.getString("name"),
                                rs.getString("suite"),
                                rs.getString("status"),
                                rs.getString("created_at"),
                                rs.getString("started_at"),
                                rs.getString("finished_at"),
                                rs.getLong("trials"),
                                rs.getLong("reached_targets"),
                                rs.getLong("optimizer_count")
                        ));
                    }
                }
            }

            long totalPages = total == 0 ? 0 : ((total + effective.size() - 1) / effective.size());
            return new PageResult<>(items, effective.page(), effective.size(), total, totalPages);
        } catch (Exception e) {
            throw new RuntimeException("Failed listing COCO campaigns", e);
        }
    }

    @Override
    public CocoCampaignDetail getCampaign(String campaignId) {
        String sql = """
                SELECT campaign_id, name, suite, dimensions_json, instances_json, functions_json,
                       status, created_at, started_at, finished_at, notes
                FROM coco_campaigns
                WHERE campaign_id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, campaignId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new CocoCampaignDetail(
                        rs.getString("campaign_id"),
                        rs.getString("name"),
                        rs.getString("suite"),
                        rs.getString("dimensions_json"),
                        rs.getString("instances_json"),
                        rs.getString("functions_json"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("started_at"),
                        rs.getString("finished_at"),
                        rs.getString("notes")
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed reading COCO campaign " + campaignId, e);
        }
    }

    @Override
    public List<CocoOptimizerConfigRow> listOptimizers(String campaignId) {
        String sql = """
                SELECT id, campaign_id, optimizer_id, config_path,
                       algorithm_type, model_type, representation_type, config_yaml, created_at
                FROM coco_optimizer_configs
                WHERE campaign_id = ?
                ORDER BY optimizer_id ASC
                """;
        List<CocoOptimizerConfigRow> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, campaignId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new CocoOptimizerConfigRow(
                            rs.getLong("id"),
                            rs.getString("campaign_id"),
                            rs.getString("optimizer_id"),
                            rs.getString("config_path"),
                            rs.getString("algorithm_type"),
                            rs.getString("model_type"),
                            rs.getString("representation_type"),
                            rs.getString("config_yaml"),
                            rs.getString("created_at")
                    ));
                }
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed listing COCO optimizer configs for " + campaignId, e);
        }
    }

    @Override
    public List<CocoAggregateMetric> listAggregates(String campaignId) {
        String sql = """
                SELECT id, campaign_id, optimizer_id, dimension, target_value,
                       mean_evals_to_target, success_rate, median_best_fitness,
                       compared_reference_optimizer, reference_ert, edaf_ert, ert_ratio, created_at
                FROM coco_aggregates
                WHERE campaign_id = ?
                ORDER BY optimizer_id ASC, dimension ASC
                """;
        List<CocoAggregateMetric> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, campaignId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new CocoAggregateMetric(
                            rs.getLong("id"),
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
                            nullableDouble(rs, "ert_ratio"),
                            rs.getString("created_at")
                    ));
                }
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed listing COCO aggregates for " + campaignId, e);
        }
    }

    @Override
    public PageResult<CocoTrialMetric> listTrials(String campaignId,
                                                  String optimizerId,
                                                  Integer functionId,
                                                  Integer dimension,
                                                  Boolean reachedTarget,
                                                  int page,
                                                  int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 500));
        int offset = safePage * safeSize;

        StringBuilder where = new StringBuilder(" WHERE campaign_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(campaignId);

        if (hasText(optimizerId)) {
            where.append(" AND LOWER(optimizer_id) = ? ");
            params.add(optimizerId.toLowerCase(Locale.ROOT));
        }
        if (functionId != null) {
            where.append(" AND function_id = ? ");
            params.add(functionId);
        }
        if (dimension != null) {
            where.append(" AND dimension = ? ");
            params.add(dimension);
        }
        if (reachedTarget != null) {
            where.append(" AND reached_target = ? ");
            params.add(reachedTarget ? 1 : 0);
        }

        String countSql = "SELECT COUNT(*) FROM coco_trials " + where;
        String dataSql = """
                SELECT id, campaign_id, optimizer_id, run_id, function_id, instance_id,
                       dimension, repetition, budget_evals, evaluations, best_fitness, runtime_millis,
                       status, reached_target, evals_to_target, target_value, created_at
                FROM coco_trials
                """ + where + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";

        try (Connection connection = dataSource.getConnection()) {
            long total = queryCount(connection, countSql, params);

            List<Object> dataParams = new ArrayList<>(params);
            dataParams.add(safeSize);
            dataParams.add(offset);

            List<CocoTrialMetric> rows = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(dataSql)) {
                bindParams(statement, dataParams);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new CocoTrialMetric(
                                rs.getLong("id"),
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
            }

            long totalPages = total == 0 ? 0 : ((total + safeSize - 1) / safeSize);
            return new PageResult<>(rows, safePage, safeSize, total, totalPages);
        } catch (Exception e) {
            throw new RuntimeException("Failed listing COCO trials for " + campaignId, e);
        }
    }

    private static CocoCampaignQuery normalize(CocoCampaignQuery query) {
        CocoCampaignQuery base = query == null ? CocoCampaignQuery.defaults() : query;
        int safePage = Math.max(0, base.page());
        int safeSize = Math.max(1, Math.min(base.size(), 200));
        String sortBy = hasText(base.sortBy()) ? base.sortBy().toLowerCase(Locale.ROOT) : "created_at";
        String sortDir = "asc".equalsIgnoreCase(base.sortDir()) ? "asc" : "desc";
        return new CocoCampaignQuery(trimToNull(base.q()), trimToNull(base.status()), trimToNull(base.suite()), safePage, safeSize, sortBy, sortDir);
    }

    private static WhereClause buildCampaignFilters(CocoCampaignQuery query) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        if (hasText(query.status())) {
            sql.append(" AND LOWER(c.status) = ? ");
            params.add(query.status().toLowerCase(Locale.ROOT));
        }
        if (hasText(query.suite())) {
            sql.append(" AND LOWER(c.suite) = ? ");
            params.add(query.suite().toLowerCase(Locale.ROOT));
        }
        if (hasText(query.q())) {
            sql.append(" AND (LOWER(c.campaign_id) LIKE ? OR LOWER(c.name) LIKE ? OR LOWER(COALESCE(c.notes,'')) LIKE ?) ");
            String like = like(query.q());
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

    private static Double nullableDouble(ResultSet rs, String column) throws Exception {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private static Long nullableLong(ResultSet rs, String column) throws Exception {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private record WhereClause(String sql, List<Object> params) {
    }
}
