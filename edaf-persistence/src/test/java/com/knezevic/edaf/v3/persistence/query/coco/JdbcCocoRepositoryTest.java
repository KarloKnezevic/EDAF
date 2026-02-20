package com.knezevic.edaf.v3.persistence.query.coco;

import com.knezevic.edaf.v3.persistence.jdbc.DataSourceFactory;
import com.knezevic.edaf.v3.persistence.jdbc.SchemaInitializer;
import com.knezevic.edaf.v3.persistence.query.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates COCO campaign query repository.
 */
class JdbcCocoRepositoryTest {

    private JdbcCocoRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        Path db = Files.createTempFile("edaf-v3-coco-repo", ".db");
        DataSource ds = DataSourceFactory.create("jdbc:sqlite:" + db, "", "");
        SchemaInitializer.initialize(ds);
        repository = new JdbcCocoRepository(ds);

        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO coco_campaigns(campaign_id, name, suite, dimensions_json, instances_json, functions_json, status, created_at, started_at)
                    VALUES
                    ('camp-1', 'Campaign One', 'bbob', '[2,5]', '[1,2]', '[1,2,3]', 'COMPLETED', '2026-02-19T10:00:00Z', '2026-02-19T10:00:01Z'),
                    ('camp-2', 'Campaign Two', 'bbob', '[10]', '[1]', '[8,15]', 'RUNNING', '2026-02-19T11:00:00Z', '2026-02-19T11:00:01Z')
                    """);

            statement.execute("""
                    INSERT INTO coco_optimizer_configs(campaign_id, optimizer_id, config_path, algorithm_type, model_type, representation_type, config_yaml, created_at)
                    VALUES
                    ('camp-1', 'gaussian', 'configs/gaussian.yml', 'gaussian-eda', 'gaussian-diag', 'real-vector', 'yaml', '2026-02-19T10:00:10Z'),
                    ('camp-1', 'cma', 'configs/cma.yml', 'cma-es', 'cma-es', 'real-vector', 'yaml', '2026-02-19T10:00:11Z')
                    """);

            statement.execute("""
                    INSERT INTO coco_trials(
                        campaign_id, optimizer_id, run_id, function_id, instance_id, dimension, repetition,
                        budget_evals, evaluations, best_fitness, runtime_millis, status, reached_target,
                        evals_to_target, target_value, created_at
                    ) VALUES
                    ('camp-1', 'gaussian', 'r1', 1, 1, 2, 1, 2000, 1800, 1.0e-9, 1200, 'COMPLETED', 1, 1600, 1.0e-8, '2026-02-19T10:01:00Z'),
                    ('camp-1', 'gaussian', 'r2', 2, 1, 2, 1, 2000, 2000, 0.1, 1300, 'COMPLETED', 0, NULL, 1.0e-8, '2026-02-19T10:02:00Z')
                    """);

            statement.execute("""
                    INSERT INTO coco_aggregates(
                        campaign_id, optimizer_id, dimension, target_value,
                        mean_evals_to_target, success_rate, median_best_fitness,
                        compared_reference_optimizer, reference_ert, edaf_ert, ert_ratio, created_at
                    ) VALUES
                    ('camp-1', 'gaussian', 2, 1.0e-8, 1600, 0.5, 0.0500000005, 'best-online', 1200, 3600, 3.0, '2026-02-19T10:05:00Z')
                    """);
        }
    }

    @Test
    void listCampaignsSupportsSearchAndCounts() {
        PageResult<CocoCampaignListItem> page = repository.listCampaigns(new CocoCampaignQuery(
                "campaign", null, "bbob", 0, 20, "created_at", "desc"
        ));

        assertEquals(2, page.total());
        assertEquals(2, page.items().size());
        assertTrue(page.items().stream().anyMatch(i -> i.campaignId().equals("camp-1") && i.trials() == 2));
    }

    @Test
    void detailAggregatesOptimizersAndTrialsAreQueryable() {
        CocoCampaignDetail detail = repository.getCampaign("camp-1");
        assertNotNull(detail);
        assertEquals("Campaign One", detail.name());

        assertEquals(2, repository.listOptimizers("camp-1").size());
        assertEquals(1, repository.listAggregates("camp-1").size());

        PageResult<CocoTrialMetric> trials = repository.listTrials("camp-1", "gaussian", null, 2, null, 0, 10);
        assertEquals(2, trials.total());
        assertEquals(2, trials.items().size());
    }
}
