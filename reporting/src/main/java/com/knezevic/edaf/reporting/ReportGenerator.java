package com.knezevic.edaf.reporting;

import com.knezevic.edaf.persistence.api.GenerationRecord;
import com.knezevic.edaf.persistence.api.RunMetadata;
import com.knezevic.edaf.persistence.api.RunResult;

import java.nio.file.Path;
import java.util.List;

/**
 * Generates a report file from algorithm run data.
 */
public interface ReportGenerator {

    /**
     * Generates a report and writes it to the output directory.
     *
     * @param metadata    run metadata
     * @param result      run result
     * @param generations per-generation records
     * @param outputDir   directory where the report file will be written
     * @return path to the generated report file
     */
    Path generate(RunMetadata metadata, RunResult result,
                  List<GenerationRecord> generations, Path outputDir);
}
