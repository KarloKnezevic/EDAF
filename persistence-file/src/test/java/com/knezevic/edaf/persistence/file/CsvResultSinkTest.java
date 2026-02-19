package com.knezevic.edaf.persistence.file;

import com.knezevic.edaf.persistence.api.GenerationRecord;
import com.knezevic.edaf.persistence.api.RunMetadata;
import com.knezevic.edaf.persistence.api.RunResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvResultSinkTest {

    @TempDir
    Path tempDir;

    @Test
    void writesCsvFileOnRunComplete() throws Exception {
        CsvResultSink sink = new CsvResultSink(tempDir);

        String runId = "test-run-csv";
        RunMetadata metadata = new RunMetadata(
            runId, "nes", "com.test.Sphere", "fp",
            100, 500, null, null, Instant.now());

        sink.onRunStarted(metadata);

        for (int i = 1; i <= 5; i++) {
            sink.onGenerationCompleted(runId, new GenerationRecord(
                i, 10.0 / i, 50.0, 25.0, 5.0,
                null, 2000000L, Instant.now()));
        }

        sink.onRunCompleted(runId, new RunResult(
            5, 2.0, null, 1000L, Instant.now()));

        Path outputFile = tempDir.resolve("run-" + runId + ".csv");
        assertTrue(Files.exists(outputFile), "CSV output file should exist");

        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(6, lines.size(), "Header + 5 data rows");
        assertEquals("generation,bestFitness,worstFitness,avgFitness,stdFitness,evalDurationNanos,recordedAt",
            lines.get(0));
        assertTrue(lines.get(1).startsWith("1,10.0,50.0,25.0,5.0,2000000,"));
        assertTrue(lines.get(5).startsWith("5,2.0,50.0,25.0,5.0,2000000,"));
    }
}
