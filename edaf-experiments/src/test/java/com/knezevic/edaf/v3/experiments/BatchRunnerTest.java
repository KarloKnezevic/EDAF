package com.knezevic.edaf.v3.experiments;

import com.knezevic.edaf.v3.experiments.runner.BatchRunner;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates repeated-run batch orchestration with deterministic run-id/seed progression.
 */
class BatchRunnerTest {

    @Test
    void batchSupportsRepetitions() throws Exception {
        Path workDir = Files.createTempDirectory("edaf-v3-batch-reps");
        Path config = workDir.resolve("mini-onemax.yml");
        Path batch = workDir.resolve("batch.yml");

        Files.writeString(config, """
                schema: "3.0"
                run:
                  id: mini-onemax
                  name: Mini OneMax
                  masterSeed: 700
                  deterministicStreams: true
                  checkpointEveryIterations: 0
                representation:
                  type: bitstring
                  length: 20
                problem:
                  type: onemax
                algorithm:
                  type: umda
                  populationSize: 50
                  selectionRatio: 0.4
                model:
                  type: umda-bernoulli
                  smoothing: 0.01
                selection:
                  type: truncation
                replacement:
                  type: elitist
                stopping:
                  type: max-iterations
                  maxIterations: 15
                constraints:
                  type: identity
                localSearch:
                  type: none
                restart:
                  type: none
                niching:
                  type: none
                observability:
                  metricsEveryIterations: 5
                  emitModelDiagnostics: true
                persistence:
                  enabled: true
                  sinks: [csv]
                  outputDirectory: %s
                  database:
                    enabled: false
                    url: jdbc:sqlite:%s
                    user: ""
                    password: ""
                reporting:
                  enabled: false
                  formats: [html]
                  outputDirectory: %s
                web:
                  enabled: false
                  port: 7070
                  pollSeconds: 3
                logging:
                  modes: [console]
                  verbosity: quiet
                  jsonlFile: %s
                  logFile: %s
                """.formatted(
                workDir.resolve("results").toString(),
                workDir.resolve("tmp.db"),
                workDir.resolve("reports").toString(),
                workDir.resolve("run.jsonl"),
                workDir.resolve("run.log")
        ));

        Files.writeString(batch, """
                defaultRepetitions: 3
                defaultSeedStart: 9100
                experiments:
                  - config: mini-onemax.yml
                    runIdPrefix: mini-onemax-stat
                """);

        BatchRunner runner = new BatchRunner();
        var results = runner.runBatch(batch, List.of());

        assertEquals(3, results.size());
        assertEquals("mini-onemax-stat-r01", results.get(0).result().runId());
        assertEquals("mini-onemax-stat-r02", results.get(1).result().runId());
        assertEquals("mini-onemax-stat-r03", results.get(2).result().runId());

        assertTrue(Files.exists(workDir.resolve("results").resolve("mini-onemax-stat-r01.csv")));
        assertTrue(Files.exists(workDir.resolve("results").resolve("mini-onemax-stat-r02.csv")));
        assertTrue(Files.exists(workDir.resolve("results").resolve("mini-onemax-stat-r03.csv")));
    }
}
