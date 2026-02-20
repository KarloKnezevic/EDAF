package com.knezevic.edaf.v3.core.config;

import com.knezevic.edaf.v3.core.errors.ConfigurationException;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Validates strict v3 loading behavior.
 */
class ConfigLoaderTest {

    @Test
    void v2ConfigIsRejected() throws Exception {
        Path file = Files.createTempFile("v2", ".yaml");
        Files.writeString(file, """
                schema-version: \"2.0\"
                problem:
                  class: com.knezevic.edaf.testing.problems.MaxOnes
                  optimization: max
                  genotype:
                    type: binary
                    length: 16
                algorithm:
                  name: umda
                  population:
                    size: 40
                  selection:
                    name: tournament
                    size: 20
                  termination:
                    max-generations: 10
                """);

        ConfigLoader loader = new ConfigLoader();
        boolean thrown = false;
        try {
            loader.load(file);
        } catch (ConfigValidationException ex) {
            thrown = true;
            assertTrue(ex.getMessage().contains("schema"));
        }
        assertTrue(thrown);
    }

    @Test
    void incompatibleModelIsRejected() throws Exception {
        Path file = Files.createTempFile("invalid", ".yaml");
        Files.writeString(file, """
                schema: \"3.0\"
                run:
                  id: test-invalid
                  masterSeed: 7
                representation:
                  type: permutation-vector
                  size: 10
                problem:
                  type: small-tsp
                algorithm:
                  type: ehm-eda
                model:
                  type: gaussian-diag
                selection:
                  type: truncation
                replacement:
                  type: elitist
                stopping:
                  type: max-iterations
                  maxIterations: 10
                constraints:
                  type: identity
                localSearch:
                  type: none
                restart:
                  type: none
                niching:
                  type: none
                observability:
                  metricsEveryIterations: 1
                  emitModelDiagnostics: true
                persistence:
                  enabled: true
                  sinks: [console]
                  outputDirectory: ./results
                  database:
                    enabled: false
                    url: jdbc:sqlite:test.db
                reporting:
                  enabled: false
                  formats: [html]
                  outputDirectory: ./reports
                web:
                  enabled: false
                  port: 7070
                  pollSeconds: 3
                logging:
                  modes: [console]
                  verbosity: normal
                """);

        ConfigLoader loader = new ConfigLoader();
        boolean thrown = false;
        try {
            loader.load(file);
        } catch (ConfigValidationException ex) {
            thrown = true;
            assertTrue(ex.getMessage().contains("model.type"));
        }
        assertTrue(thrown);
    }

    @Test
    void batchConfigIsDetectedAndLoaded() throws Exception {
        Path file = Files.createTempFile("batch", ".yaml");
        Files.writeString(file, """
                experiments:
                  - umda-onemax-v3.yml
                  - gaussian-sphere-v3.yml
                """);

        ConfigLoader loader = new ConfigLoader();
        assertEquals(ConfigDocumentType.BATCH, loader.detectType(file));
        BatchConfig batch = loader.loadBatch(file);
        assertEquals(2, batch.getExperiments().size());
        assertEquals("umda-onemax-v3.yml", batch.getExperiments().get(0).getConfig());
        assertEquals(1, batch.getExperiments().get(0).getRepetitions());
    }

    @Test
    void batchConfigSupportsRepetitionsAndSeedRanges() throws Exception {
        Path file = Files.createTempFile("batch-advanced", ".yaml");
        Files.writeString(file, """
                defaultRepetitions: 30
                defaultSeedStart: 8100
                experiments:
                  - config: umda-onemax-v3.yml
                    runIdPrefix: sig-umda
                  - config: gaussian-sphere-v3.yml
                    repetitions: 10
                    seedStart: 12000
                    runIdPrefix: sig-gaussian
                """);

        ConfigLoader loader = new ConfigLoader();
        BatchConfig batch = loader.loadBatch(file);
        assertEquals(2, batch.getExperiments().size());
        assertEquals(30, batch.getExperiments().get(0).getRepetitions());
        assertEquals(8100L, batch.getExperiments().get(0).getSeedStart());
        assertEquals(10, batch.getExperiments().get(1).getRepetitions());
        assertEquals(12000L, batch.getExperiments().get(1).getSeedStart());
    }

    @Test
    void batchConfigRejectsUnknownFields() throws Exception {
        Path file = Files.createTempFile("batch-invalid", ".yaml");
        Files.writeString(file, """
                experiments:
                  - config: umda-onemax-v3.yml
                    foo: bar
                """);

        ConfigLoader loader = new ConfigLoader();
        assertThrows(ConfigurationException.class, () -> loader.loadBatch(file));
    }
}
