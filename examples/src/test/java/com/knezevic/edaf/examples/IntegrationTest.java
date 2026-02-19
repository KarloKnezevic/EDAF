package com.knezevic.edaf.examples;

import com.knezevic.edaf.configuration.ConfigurationLoader;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.factory.ComponentFactory;
import com.knezevic.edaf.factory.SpiBackedComponentFactory;
import com.knezevic.edaf.persistence.api.GenerationRecord;
import com.knezevic.edaf.persistence.api.PersistenceEventPublisher;
import com.knezevic.edaf.persistence.api.RunResult;
import com.knezevic.edaf.persistence.file.FileResultSink;
import com.knezevic.edaf.persistence.jdbc.DataSourceFactory;
import com.knezevic.edaf.persistence.jdbc.JdbcResultSink;
import com.knezevic.edaf.persistence.jdbc.JdbcResultStore;
import com.knezevic.edaf.persistence.jdbc.SchemaInitializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests: load YAML config, run algorithm, verify persistence outputs.
 */
class IntegrationTest {

    @Test
    void cemSphereWithFilePersistence(@TempDir Path tempDir) throws Exception {
        String configPath = getClass().getClassLoader().getResource("integration-cem-sphere.yaml").getPath();
        runAlgorithmWithFilePersistence(configPath, tempDir, "json");

        // Verify JSON output file was created
        long jsonFiles = Files.list(tempDir)
            .filter(p -> p.toString().endsWith(".json"))
            .count();
        assertTrue(jsonFiles >= 1, "Expected at least one JSON output file in " + tempDir);
    }

    @Test
    void cemSphereWithCsvPersistence(@TempDir Path tempDir) throws Exception {
        String configPath = getClass().getClassLoader().getResource("integration-cem-sphere.yaml").getPath();
        runAlgorithmWithFilePersistence(configPath, tempDir, "csv");

        long csvFiles = Files.list(tempDir)
            .filter(p -> p.toString().endsWith(".csv"))
            .count();
        assertTrue(csvFiles >= 1, "Expected at least one CSV output file in " + tempDir);
    }

    @Test
    void cemSphereWithJdbcPersistence(@TempDir Path tempDir) throws Exception {
        String configPath = getClass().getClassLoader().getResource("integration-cem-sphere.yaml").getPath();
        String dbUrl = "jdbc:sqlite:" + tempDir.resolve("test.db");

        DataSource ds = DataSourceFactory.create(dbUrl, null, null);
        SchemaInitializer.initialize(ds);
        JdbcResultSink jdbcSink = new JdbcResultSink(ds);
        JdbcResultStore store = new JdbcResultStore(ds);

        runAlgorithmWithSink(configPath, jdbcSink);

        // Verify data was persisted to SQLite
        var runs = store.listRuns();
        assertFalse(runs.isEmpty(), "Expected at least one run in the database");

        var runId = runs.get(0).runId();
        var generations = store.getGenerations(runId);
        assertFalse(generations.isEmpty(), "Expected generation records in the database");

        var result = store.getResult(runId);
        assertTrue(result.isPresent(), "Expected a run result in the database");
        assertTrue(result.get().totalGenerations() > 0);
    }

    @Test
    void nesSphereConverges(@TempDir Path tempDir) throws Exception {
        String configPath = getClass().getClassLoader().getResource("integration-nes-sphere.yaml").getPath();

        // Run NES and verify it converges (fitness decreases)
        String dbUrl = "jdbc:sqlite:" + tempDir.resolve("nes-test.db");
        DataSource ds = DataSourceFactory.create(dbUrl, null, null);
        SchemaInitializer.initialize(ds);
        JdbcResultSink jdbcSink = new JdbcResultSink(ds);
        JdbcResultStore store = new JdbcResultStore(ds);

        runAlgorithmWithSink(configPath, jdbcSink);

        var runs = store.listRuns();
        assertFalse(runs.isEmpty());

        var generations = store.getGenerations(runs.get(0).runId());
        assertTrue(generations.size() >= 2, "Expected multiple generation records");

        // First generation fitness should be worse than last
        GenerationRecord first = generations.get(0);
        GenerationRecord last = generations.get(generations.size() - 1);
        assertTrue(last.bestFitness() <= first.bestFitness(),
            "NES should improve fitness (minimize). First: " + first.bestFitness() + ", Last: " + last.bestFitness());
    }

    @Test
    void umdaMaxOnesConverges(@TempDir Path tempDir) throws Exception {
        String configPath = getClass().getClassLoader().getResource("integration-umda-maxones.yaml").getPath();

        String dbUrl = "jdbc:sqlite:" + tempDir.resolve("umda-test.db");
        DataSource ds = DataSourceFactory.create(dbUrl, null, null);
        SchemaInitializer.initialize(ds);
        JdbcResultSink jdbcSink = new JdbcResultSink(ds);
        JdbcResultStore store = new JdbcResultStore(ds);

        runAlgorithmWithSink(configPath, jdbcSink);

        var runs = store.listRuns();
        assertFalse(runs.isEmpty());

        var resultOpt = store.getResult(runs.get(0).runId());
        assertTrue(resultOpt.isPresent());
        RunResult result = resultOpt.get();
        // UMDA on 20-bit MaxOnes with 100 pop, 50 gens should get close to 20
        assertTrue(result.bestFitness() >= 15,
            "UMDA should achieve good fitness on MaxOnes, got: " + result.bestFitness());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void runAlgorithmWithFilePersistence(String configPath, Path outputDir, String format) throws Exception {
        FileResultSink fileSink = new FileResultSink(outputDir, format);
        runAlgorithmWithSink(configPath, fileSink);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void runAlgorithmWithSink(String configPath, com.knezevic.edaf.persistence.api.ResultSink sink) throws Exception {
        ConfigurationLoader loader = new ConfigurationLoader();
        Configuration config = loader.load(configPath);

        ComponentFactory factory = new SpiBackedComponentFactory();
        Random random = new Random(42);

        Problem problem = factory.createProblem(config);
        Genotype genotype = factory.createGenotype(config, random);
        Population population = null;
        if (config.getAlgorithm().getPopulation() != null) {
            population = factory.createPopulation(config, genotype);
        }
        Statistics statistics = factory.createStatistics(config, genotype, random);
        Selection selection = null;
        if (config.getAlgorithm().getSelection() != null) {
            selection = factory.createSelection(config, random);
        }
        TerminationCondition terminationCondition = factory.createTerminationCondition(config);
        Algorithm algorithm = factory.createAlgorithm(config, problem, population, selection, statistics, terminationCondition, random);

        assertNotNull(algorithm, "Algorithm should be created from config");

        // Set up event publishing with persistence
        var composite = new com.knezevic.edaf.core.runtime.CompositeEventPublisher();
        composite.addPublisher(new PersistenceEventPublisher(sink));

        var rs = new com.knezevic.edaf.core.runtime.SplittableRandomSource(42L);
        var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();
        var ctx = new com.knezevic.edaf.core.runtime.ExecutionContext(rs, executor, composite);

        if (algorithm instanceof com.knezevic.edaf.core.runtime.SupportsExecutionContext s) {
            s.setExecutionContext(ctx);
        }

        algorithm.run();

        assertNotNull(algorithm.getBest(), "Algorithm should produce a best individual");
        executor.shutdown();
    }
}
