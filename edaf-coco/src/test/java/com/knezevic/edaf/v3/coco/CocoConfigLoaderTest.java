package com.knezevic.edaf.v3.coco;

import com.knezevic.edaf.v3.coco.config.CocoConfigLoader;
import com.knezevic.edaf.v3.coco.config.CocoConfigValidationException;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for COCO campaign config parsing and semantic validation.
 */
class CocoConfigLoaderTest {

    @Test
    void loadsValidCampaignConfig() throws Exception {
        Path file = Files.createTempFile("edaf-coco-valid", ".yml");
        Files.writeString(file, """
                schema: "3.0-coco"
                campaign:
                  id: smoke
                  name: Smoke campaign
                  suite: bbob
                  functions: [1, 2]
                  dimensions: [2, 5]
                  instances: [1]
                  repetitions: 1
                  maxEvaluationsMultiplier: 100
                  targetFitness: 1.0e-8
                  databaseUrl: jdbc:sqlite:edaf-v3.db
                  databaseUser: ""
                  databasePassword: ""
                  outputDirectory: ./results/coco
                  reportDirectory: ./reports/coco
                  referenceMode: best-online
                optimizers:
                  - id: gauss
                    config: ../configs/gaussian-sphere-v3.yml
                """);

        CocoConfigLoader loader = new CocoConfigLoader();
        var config = loader.load(file);
        assertEquals("smoke", config.getCampaign().getId());
        assertEquals(1, config.getOptimizers().size());
    }

    @Test
    void rejectsInvalidFunctionId() throws Exception {
        Path file = Files.createTempFile("edaf-coco-invalid", ".yml");
        Files.writeString(file, """
                schema: "3.0-coco"
                campaign:
                  id: bad
                  name: Bad campaign
                  suite: bbob
                  functions: [0]
                  dimensions: [2]
                  instances: [1]
                  repetitions: 1
                  maxEvaluationsMultiplier: 100
                  targetFitness: 1.0e-8
                  databaseUrl: jdbc:sqlite:edaf-v3.db
                  databaseUser: ""
                  databasePassword: ""
                  outputDirectory: ./results/coco
                  reportDirectory: ./reports/coco
                  referenceMode: best-online
                optimizers:
                  - id: gauss
                    config: ../configs/gaussian-sphere-v3.yml
                """);

        CocoConfigLoader loader = new CocoConfigLoader();
        assertThrows(CocoConfigValidationException.class, () -> loader.load(file));
    }
}
