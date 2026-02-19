package com.knezevic.edaf.v3.reporting;

import com.knezevic.edaf.v3.persistence.query.RunRepository;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestrates report generation for one run across requested formats.
 */
public final class ReportService {

    private final RunRepository repository;
    private final Map<String, ReportGenerator> generators;

    public ReportService(RunRepository repository, List<ReportGenerator> generators) {
        this.repository = repository;
        this.generators = generators.stream().collect(Collectors.toMap(ReportGenerator::format, g -> g));
    }

    /**
     * Generates all requested formats for a run.
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
