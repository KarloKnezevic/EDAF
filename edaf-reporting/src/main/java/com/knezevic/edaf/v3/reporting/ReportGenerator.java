package com.knezevic.edaf.v3.reporting;

import com.knezevic.edaf.v3.persistence.query.IterationMetric;
import com.knezevic.edaf.v3.persistence.query.RunSummary;

import java.nio.file.Path;
import java.util.List;

/**
 * Contract for report generators.
 */
public interface ReportGenerator {

    /**
     * Generates a report file for one run.
     */
    Path generate(RunSummary run, List<IterationMetric> iterations, Path outputDir);

    /**
     * Report format id (html, latex, ...).
     */
    String format();
}
