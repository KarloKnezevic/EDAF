package com.knezevic.edaf.persistence.file;

import com.knezevic.edaf.persistence.api.GenerationRecord;
import com.knezevic.edaf.persistence.api.RunMetadata;
import com.knezevic.edaf.persistence.api.RunResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JsonResultSinkTest {

    @TempDir
    Path tempDir;

    @Test
    void writesJsonFileOnRunComplete() throws Exception {
        JsonResultSink sink = new JsonResultSink(tempDir);

        String runId = "test-run-1";
        RunMetadata metadata = new RunMetadata(
            runId, "cem", "com.test.Sphere", "fp",
            50, 100, null, 12345L, Instant.now());

        sink.onRunStarted(metadata);

        for (int i = 1; i <= 3; i++) {
            sink.onGenerationCompleted(runId, new GenerationRecord(
                i, 10.0 - i, 20.0, 15.0, 3.0,
                "[1.0, 2.0]", 1000000L, Instant.now()));
        }

        sink.onRunCompleted(runId, new RunResult(
            3, 7.0, "[1.0, 2.0]", 500L, Instant.now()));

        Path outputFile = tempDir.resolve("run-" + runId + ".json");
        assertTrue(Files.exists(outputFile), "JSON output file should exist");

        String content = Files.readString(outputFile);
        assertTrue(content.contains("\"runId\" : \"test-run-1\""));
        assertTrue(content.contains("\"algorithmId\" : \"cem\""));
        assertTrue(content.contains("\"totalGenerations\" : 3"));
        assertTrue(content.contains("\"bestFitness\" : 7.0"));
        assertTrue(content.contains("\"generations\""));
    }
}
