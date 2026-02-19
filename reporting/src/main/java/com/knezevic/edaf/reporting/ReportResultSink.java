package com.knezevic.edaf.reporting;

import com.knezevic.edaf.persistence.api.GenerationRecord;
import com.knezevic.edaf.persistence.api.ResultSink;
import com.knezevic.edaf.persistence.api.RunMetadata;
import com.knezevic.edaf.persistence.api.RunResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ResultSink} that collects generation records during a run
 * and triggers report generation on completion.
 */
public class ReportResultSink implements ResultSink {

    private final ReportGenerator generator;
    private final Path outputDir;

    private RunMetadata metadata;
    private final List<GenerationRecord> generations = new ArrayList<>();

    public ReportResultSink(ReportGenerator generator, Path outputDir) {
        this.generator = generator;
        this.outputDir = outputDir;
    }

    @Override
    public void onRunStarted(RunMetadata metadata) {
        this.metadata = metadata;
        this.generations.clear();
    }

    @Override
    public void onGenerationCompleted(String runId, GenerationRecord record) {
        generations.add(record);
    }

    @Override
    public void onRunCompleted(String runId, RunResult result) {
        generator.generate(metadata, result, generations, outputDir);
    }
}
