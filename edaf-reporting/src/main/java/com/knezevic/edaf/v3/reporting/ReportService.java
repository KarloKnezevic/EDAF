/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.reporting;

import com.knezevic.edaf.v3.persistence.query.RunRepository;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestrates report generation for one run across requested formats.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class ReportService {

    private final RunRepository repository;
    private final Map<String, ReportGenerator> generators;

    /**
     * Creates a new ReportService instance.
     *
     * @param repository repository dependency
     * @param generators the generators argument
     */
    public ReportService(RunRepository repository, List<ReportGenerator> generators) {
        this.repository = repository;
        this.generators = generators.stream().collect(Collectors.toMap(ReportGenerator::format, g -> g));
    }

    /**
     * Generates all requested formats for a run.
     * @param runId run identifier
     * @param outputDir output directory path
     * @param formats report format list
     * @return generated artifact path
     */
    public Map<String, Path> generate(String runId, Path outputDir, List<String> formats) {
        var run = repository.getRun(runId);
        if (run == null) {
            throw new IllegalArgumentException("Run not found: " + runId);
        }
        var iterations = repository.listIterations(runId);

        java.util.LinkedHashMap<String, Path> artifacts = new java.util.LinkedHashMap<>();
        for (String format : formats) {
            ReportGenerator generator = generators.get(format);
            if (generator == null) {
                throw new IllegalArgumentException("Unsupported report format: " + format);
            }
            artifacts.put(format, generator.generate(run, iterations, outputDir));
        }
        return artifacts;
    }
}
