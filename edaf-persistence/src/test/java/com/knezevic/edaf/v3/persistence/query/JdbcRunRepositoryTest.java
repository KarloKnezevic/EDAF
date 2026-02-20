package com.knezevic.edaf.v3.persistence.query;

import com.knezevic.edaf.v3.persistence.jdbc.DataSourceFactory;
import com.knezevic.edaf.v3.persistence.jdbc.SchemaInitializer;
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
 * Validates filtering/sorting/pagination and detail queries.
 */
class JdbcRunRepositoryTest {

    private JdbcRunRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        Path db = Files.createTempFile("edaf-v3-repo", ".db");
        DataSource ds = DataSourceFactory.create("jdbc:sqlite:" + db, "", "");
        SchemaInitializer.initialize(ds);
        repository = new JdbcRunRepository(ds);

        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO experiments(
                        experiment_id, config_hash, schema_version, run_name, algorithm_type, model_type,
                        problem_type, representation_type, selection_type, replacement_type, stopping_type,
                        max_iterations, config_yaml, config_json, created_at
                    ) VALUES
                    ('exp-1', 'hash-aaa', '3.0', 'Exp A', 'umda', 'umda-bernoulli', 'onemax', 'bitstring', 'truncation', 'elitist', 'max-iterations', 80, 'yaml-a', '{"problem":{"type":"onemax"}}', '2026-02-01T10:00:00Z'),
                    ('exp-2', 'hash-bbb', '3.0', 'Exp B', 'gaussian-eda', 'gaussian-diag', 'sphere', 'real-vector', 'truncation', 'elitist', 'max-iterations', 90, 'yaml-b', '{"problem":{"type":"sphere"}}', '2026-02-02T10:00:00Z'),
                    ('exp-3', 'hash-ccc', '3.0', 'Exp C', 'pbil', 'pbil-frequency', 'onemax', 'bitstring', 'truncation', 'elitist', 'max-iterations', 80, 'yaml-c', '{"problem":{"type":"onemax"}}', '2026-02-03T10:00:00Z')
                    """);

            statement.execute("""
                    INSERT INTO experiment_params(experiment_id, section, param_path, leaf_key, value_type, value_text)
                    VALUES
                    ('exp-1', 'problem', 'problem.genotype.maxDepth', 'maxDepth', 'number', '6'),
                    ('exp-1', 'problem', 'problem.criteria[0]', '0', 'string', 'nonlinearity')
                    """);

            statement.execute("""
                    INSERT INTO runs(
                        run_id, experiment_id, seed, status, start_time, end_time,
                        iterations, evaluations, best_fitness, best_summary, runtime_millis
                    ) VALUES
                    ('run-1', 'exp-1', 11, 'COMPLETED', '2026-02-10T09:00:00Z', '2026-02-10T09:02:00Z', 80, 9600, 64.0, '1111', 120000),
                    ('run-2', 'exp-2', 22, 'FAILED', '2026-02-11T09:00:00Z', '2026-02-11T09:00:10Z', 3, 300, 12.3, 'vec', 10000),
                    ('run-3', 'exp-1', 12, 'COMPLETED', '2026-02-10T10:00:00Z', '2026-02-10T10:02:00Z', 80, 8700, 61.0, '1011', 111000),
                    ('run-4', 'exp-1', 13, 'FAILED', '2026-02-10T11:00:00Z', '2026-02-10T11:01:00Z', 12, 1500, 44.0, '0001', 60000),
                    ('run-5', 'exp-3', 11, 'COMPLETED', '2026-02-12T09:00:00Z', '2026-02-12T09:01:50Z', 80, 9900, 59.0, '0111', 110000),
                    ('run-6', 'exp-3', 12, 'COMPLETED', '2026-02-12T10:00:00Z', '2026-02-12T10:01:55Z', 80, 9400, 57.5, '0011', 115000),
                    ('run-7', 'exp-3', 13, 'COMPLETED', '2026-02-12T11:00:00Z', '2026-02-12T11:02:15Z', 80, 9800, 60.0, '0010', 135000)
                    """);

            statement.execute("""
                    INSERT INTO iterations(run_id, iteration, evaluations, best_fitness, mean_fitness, std_fitness, metrics_json, diagnostics_json, created_at)
                    VALUES
                    ('run-1', 1, 120, 10.0, 5.0, 1.0, '{"entropy":0.5}', '{"cond":1.2}', '2026-02-10T09:00:05Z'),
                    ('run-1', 2, 240, 18.0, 9.0, 1.2, '{"entropy":0.4}', '{"cond":1.1}', '2026-02-10T09:00:10Z')
                    """);

            statement.execute("""
                    INSERT INTO checkpoints(run_id, iteration, checkpoint_path, created_at)
                    VALUES ('run-1', 40, '/tmp/run-1-40.ckpt.yaml', '2026-02-10T09:01:00Z')
                    """);

            statement.execute("""
                    INSERT INTO events(run_id, event_type, payload_json, created_at)
                    VALUES
                    ('run-1', 'iteration_completed', '{"metrics":{"entropy":0.5}}', '2026-02-10T09:00:05Z'),
                    ('run-1', 'run_completed', '{"status":"done"}', '2026-02-10T09:02:00Z')
                    """);
        }
    }

    @Test
    void listRunsSupportsFilterSearchSortAndPaging() {
        PageResult<RunListItem> page = repository.listRuns(new RunQuery(
                "maxDepth", "umda", null, null, "COMPLETED", null, null, null, null,
                0, 10, "best_fitness", "desc"
        ));

        assertEquals(2, page.items().size());
        assertEquals("run-1", page.items().get(0).runId());
        assertEquals(2, page.total());
    }

    @Test
    void listExperimentsSupportsFilterSearchSortAndPaging() {
        PageResult<ExperimentListItem> page = repository.listExperiments(new ExperimentQuery(
                "maxDepth", "umda", null, null, null, null, 0, 10, "created_at", "asc"
        ));

        assertEquals(1, page.items().size());
        assertEquals("exp-1", page.items().getFirst().experimentId());
        assertEquals(3, page.items().getFirst().totalRuns());
        assertEquals(2, page.items().getFirst().completedRuns());
    }

    @Test
    void detailAndChildrenQueriesReturnExpectedRows() {
        RunDetail detail = repository.getRunDetail("run-1");
        assertNotNull(detail);
        assertEquals("exp-1", detail.experimentId());
        assertEquals("hash-aaa", detail.configHash());

        assertEquals(2, repository.listIterations("run-1").size());
        assertEquals(1, repository.listCheckpoints("run-1").size());
        assertTrue(repository.listExperimentParams("run-1").stream()
                .anyMatch(p -> "problem.genotype.maxDepth".equals(p.paramPath())));
    }

    @Test
    void eventsAndFacetsQueriesWork() {
        PageResult<EventRow> events = repository.listEvents("run-1", "iteration_completed", "entropy", 0, 20);
        assertEquals(1, events.items().size());

        FilterFacets facets = repository.listFacets();
        assertTrue(facets.algorithms().contains("umda"));
        assertTrue(facets.models().contains("gaussian-diag"));
        assertTrue(facets.statuses().contains("FAILED"));
    }

    @Test
    void experimentQueriesAndAnalyticsWork() {
        ExperimentDetail detail = repository.getExperimentDetail("exp-1");
        assertNotNull(detail);
        assertEquals(3, detail.totalRuns());
        assertEquals(2, detail.completedRuns());

        PageResult<ExperimentRunItem> page = repository.listExperimentRuns("exp-1", 0, 10, "start_time", "asc");
        assertEquals(3, page.items().size());
        assertEquals("run-1", page.items().get(0).runId());

        ExperimentAnalytics analytics = repository.analyzeExperiment("exp-1", "max", 60.0);
        assertEquals(3, analytics.totalRuns());
        assertEquals(2, analytics.successfulRuns());
        assertTrue(analytics.successRate() > 0.6);
        assertNotNull(analytics.bestFitnessBox().median());
    }

    @Test
    void problemComparisonBuildsSignificanceAndProfiles() {
        ProblemComparisonReport report = repository.compareAlgorithmsOnProblem("onemax", "max", 58.0, null);
        assertEquals("onemax", report.problemType());
        assertTrue(report.algorithms().size() >= 2);
        assertTrue(report.pairwiseWilcoxon().size() >= 1);
        assertNotNull(report.friedman());
        assertTrue(report.dataProfiles().size() >= 2);
        assertTrue(report.performanceProfiles().size() >= 2);
    }
}
