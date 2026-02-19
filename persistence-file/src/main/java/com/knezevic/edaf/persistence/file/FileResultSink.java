package com.knezevic.edaf.persistence.file;

import com.knezevic.edaf.persistence.api.GenerationRecord;
import com.knezevic.edaf.persistence.api.ResultSink;
import com.knezevic.edaf.persistence.api.RunMetadata;
import com.knezevic.edaf.persistence.api.RunResult;

import java.nio.file.Path;

/**
 * Delegates to either {@link JsonResultSink} or {@link CsvResultSink}
 * based on the configured format.
 */
public class FileResultSink implements ResultSink {

    private final ResultSink delegate;

    public FileResultSink(Path directory, String format) {
        this.delegate = switch (format.toLowerCase()) {
            case "csv" -> new CsvResultSink(directory);
            default -> new JsonResultSink(directory);
        };
    }

    @Override
    public void onRunStarted(RunMetadata metadata) {
        delegate.onRunStarted(metadata);
    }

    @Override
    public void onGenerationCompleted(String runId, GenerationRecord record) {
        delegate.onGenerationCompleted(runId, record);
    }

    @Override
    public void onRunCompleted(String runId, RunResult result) {
        delegate.onRunCompleted(runId, result);
    }
}
