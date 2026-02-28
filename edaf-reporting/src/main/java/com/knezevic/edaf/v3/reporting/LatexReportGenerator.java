/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.reporting;

import com.knezevic.edaf.v3.persistence.query.IterationMetric;
import com.knezevic.edaf.v3.persistence.query.RunSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Generates compact LaTeX report source for reproducible papers.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class LatexReportGenerator implements ReportGenerator {

    /**
     * Generates report artifact.
     *
     * @param run the run argument
     * @param iterations the iterations argument
     * @param outputDir output directory path
     * @return generated artifact path
     */
    @Override
    public Path generate(RunSummary run, List<IterationMetric> iterations, Path outputDir) {
        try {
            Files.createDirectories(outputDir);
            Path out = outputDir.resolve("report-" + run.runId() + ".tex");
            String tex = """
                    \\documentclass{article}
                    \\usepackage[margin=1in]{geometry}
                    \\begin{document}
                    \\section*{EDAF v3 Run Report}
                    \\textbf{Run ID:} %s \\\\
                    \\textbf{Algorithm:} %s \\\\
                    \\textbf{Model:} %s \\\\
                    \\textbf{Problem:} %s \\\\
                    \\textbf{Status:} %s \\\\
                    \\textbf{Best Fitness:} %s \\\\
                    \\textbf{Runtime (ms):} %s

                    \\subsection*{Final Iteration}
                    %s

                    \\end{document}
                    """.formatted(
                    run.runId(),
                    run.algorithm(),
                    run.model(),
                    run.problem(),
                    run.status() == null ? "UNKNOWN" : run.status(),
                    run.bestFitness() == null ? "n/a" : run.bestFitness(),
                    run.runtimeMillis() == null ? "n/a" : run.runtimeMillis(),
                    iterations.isEmpty() ? "No iterations available." :
                            "Iteration " + iterations.get(iterations.size() - 1).iteration() +
                                    ", best=" + iterations.get(iterations.size() - 1).bestFitness()
            );
            Files.writeString(out, tex);
            return out;
        } catch (IOException e) {
            throw new RuntimeException("Failed generating LaTeX report", e);
        }
    }

    /**
     * Executes format.
     *
     * @return the format
     */
    @Override
    public String format() {
        return "latex";
    }
}
