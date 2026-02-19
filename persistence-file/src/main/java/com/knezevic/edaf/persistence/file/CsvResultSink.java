package com.knezevic.edaf.persistence.file;

import com.knezevic.edaf.persistence.api.GenerationRecord;
import com.knezevic.edaf.persistence.api.ResultSink;
import com.knezevic.edaf.persistence.api.RunMetadata;
import com.knezevic.edaf.persistence.api.RunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes algorithm run results as a CSV file with one row per generation.
 * <p>
 * Output file: {@code <directory>/run-<runId>.csv}
 * </p>
 */
public class CsvResultSink implements ResultSink {

    private static final Logger log = LoggerFactory.getLogger(CsvResultSink.class);
    private static final String HEADER = "generation,bestFitness,worstFitness,avgFitness,stdFitness,evalDurationNanos,recordedAt";

    private final Path directory;
    private BufferedWriter writer;
    private String currentRunId;

    public CsvResultSink(Path directory) {
        this.directory = directory;
    }

    @Override
    public void onRunStarted(RunMetadata metadata) {
        try {
            Files.createDirectories(directory);
            currentRunId = metadata.runId();
            Path file = directory.resolve("run-" + currentRunId + ".csv");
            writer = Files.newBufferedWriter(file);
            writer.write(HEADER);
            writer.newLine();
        } catch (IOException e) {
            log.error("Failed to initialize CSV file for run {}", metadata.runId(), e);
        }
    }

    @Override
    public void onGenerationCompleted(String runId, GenerationRecord record) {
        if (writer == null) return;
        try {
            writer.write(String.format("%d,%s,%s,%s,%s,%d,%s",
                record.generation(),
                formatDouble(record.bestFitness()),
                formatDouble(record.worstFitness()),
                formatDouble(record.avgFitness()),
                formatDouble(record.stdFitness()),
                record.evalDurationNanos(),
                record.recordedAt().toString()));
            writer.newLine();
        } catch (IOException e) {
            log.error("Failed to write CSV row for generation {}", record.generation(), e);
        }
    }

    @Override
    public void onRunCompleted(String runId, RunResult result) {
        if (writer == null) return;
        try {
            writer.flush();
            writer.close();
            Path file = directory.resolve("run-" + currentRunId + ".csv");
            log.info("Run results written to {}", file);
        } catch (IOException e) {
            log.error("Failed to close CSV file for run {}", runId, e);
        } finally {
            writer = null;
        }
    }

    private static String formatDouble(double value) {
        if (Double.isNaN(value)) return "NaN";
        return String.valueOf(value);
    }
}
