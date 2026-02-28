/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.reporting;

import com.knezevic.edaf.v3.persistence.query.IterationMetric;
import com.knezevic.edaf.v3.persistence.query.RunSummary;

import java.nio.file.Path;
import java.util.List;

/**
 * Contract for report generators.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
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
