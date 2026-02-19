package com.knezevic.edaf.reporting;

import com.knezevic.edaf.persistence.api.GenerationRecord;
import com.knezevic.edaf.persistence.api.RunMetadata;
import com.knezevic.edaf.persistence.api.RunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Generates a self-contained HTML report with inline CSS and SVG convergence chart.
 */
public class HtmlReportGenerator implements ReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(HtmlReportGenerator.class);

    @Override
    public Path generate(RunMetadata metadata, RunResult result,
                         List<GenerationRecord> generations, Path outputDir) {
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            log.error("Failed to create report directory: {}", outputDir, e);
            return null;
        }

        String chart = SvgChartGenerator.generateConvergenceChart(generations);
        String html = buildHtml(metadata, result, generations, chart);

        Path file = outputDir.resolve("report-" + metadata.runId() + ".html");
        try {
            Files.writeString(file, html);
            log.info("HTML report written to {}", file);
            return file;
        } catch (IOException e) {
            log.error("Failed to write HTML report to {}", file, e);
            return null;
        }
    }

    private String buildHtml(RunMetadata metadata, RunResult result,
                             List<GenerationRecord> generations, String chart) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>EDAF Run Report - ").append(metadata.algorithmId()).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; ");
        html.append("margin: 40px auto; max-width: 900px; color: #333; line-height: 1.6; }\n");
        html.append("h1 { color: #1976D2; border-bottom: 2px solid #1976D2; padding-bottom: 8px; }\n");
        html.append("h2 { color: #455A64; margin-top: 32px; }\n");
        html.append("table { border-collapse: collapse; width: 100%; margin: 16px 0; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 8px 12px; text-align: right; }\n");
        html.append("th { background: #f5f5f5; text-align: center; }\n");
        html.append("tr:nth-child(even) { background: #fafafa; }\n");
        html.append(".meta-table td:first-child { text-align: left; font-weight: 600; width: 200px; }\n");
        html.append(".meta-table td:last-child { text-align: left; }\n");
        html.append(".summary { background: #E3F2FD; border-radius: 8px; padding: 16px 24px; margin: 16px 0; }\n");
        html.append(".summary strong { color: #1565C0; }\n");
        html.append("footer { margin-top: 40px; color: #999; font-size: 12px; }\n");
        html.append("</style>\n</head>\n<body>\n");

        // Title
        html.append("<h1>EDAF Run Report</h1>\n");

        // Summary box
        html.append("<div class=\"summary\">\n");
        html.append(String.format(Locale.US, "<p><strong>Algorithm:</strong> %s | ", metadata.algorithmId()));
        html.append(String.format(Locale.US, "<strong>Best Fitness:</strong> %.6f | ", result.bestFitness()));
        html.append(String.format("<strong>Generations:</strong> %d | ", result.totalGenerations()));
        html.append(String.format("<strong>Duration:</strong> %d ms</p>\n", result.totalDurationMillis()));
        html.append("</div>\n");

        // Metadata table
        html.append("<h2>Run Metadata</h2>\n");
        html.append("<table class=\"meta-table\">\n");
        html.append(metaRow("Run ID", metadata.runId()));
        html.append(metaRow("Algorithm", metadata.algorithmId()));
        html.append(metaRow("Problem", metadata.problemClass()));
        html.append(metaRow("Genotype", metadata.genotypeType()));
        html.append(metaRow("Population Size", String.valueOf(metadata.populationSize())));
        html.append(metaRow("Max Generations", String.valueOf(metadata.maxGenerations())));
        if (metadata.seed() != null) {
            html.append(metaRow("Seed", String.valueOf(metadata.seed())));
        }
        html.append(metaRow("Started", metadata.startedAt().toString()));
        html.append(metaRow("Completed", result.completedAt().toString()));
        html.append("</table>\n");

        // Convergence chart
        if (!chart.isEmpty()) {
            html.append("<h2>Convergence Chart</h2>\n");
            html.append(chart).append("\n");
        }

        // Generation table (sample if too many)
        html.append("<h2>Generation Statistics</h2>\n");
        html.append("<table>\n");
        html.append("<tr><th>Gen</th><th>Best</th><th>Worst</th><th>Avg</th><th>Std</th></tr>\n");
        int step = Math.max(1, generations.size() / 50); // Show max ~50 rows
        for (int i = 0; i < generations.size(); i += step) {
            GenerationRecord g = generations.get(i);
            html.append(String.format(Locale.US, "<tr><td>%d</td><td>%.6f</td><td>%.6f</td><td>%.6f</td><td>%.6f</td></tr>\n",
                g.generation(), g.bestFitness(), g.worstFitness(), g.avgFitness(), g.stdFitness()));
        }
        // Always include last generation
        if (step > 1 && !generations.isEmpty()) {
            GenerationRecord last = generations.get(generations.size() - 1);
            html.append(String.format(Locale.US, "<tr><td>%d</td><td>%.6f</td><td>%.6f</td><td>%.6f</td><td>%.6f</td></tr>\n",
                last.generation(), last.bestFitness(), last.worstFitness(), last.avgFitness(), last.stdFitness()));
        }
        html.append("</table>\n");

        html.append("<footer>Generated by EDAF Framework v2.0</footer>\n");
        html.append("</body>\n</html>");
        return html.toString();
    }

    private String metaRow(String label, String value) {
        return String.format("<tr><td>%s</td><td>%s</td></tr>\n", label, value);
    }
}
