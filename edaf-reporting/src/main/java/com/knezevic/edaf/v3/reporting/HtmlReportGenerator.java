package com.knezevic.edaf.v3.reporting;

import com.knezevic.edaf.v3.persistence.query.IterationMetric;
import com.knezevic.edaf.v3.persistence.query.RunSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Generates standalone HTML report with embedded Chart.js visualizations.
 */
public final class HtmlReportGenerator implements ReportGenerator {

    @Override
    public Path generate(RunSummary run, List<IterationMetric> iterations, Path outputDir) {
        try {
            Files.createDirectories(outputDir);
            Path out = outputDir.resolve("report-" + run.runId() + ".html");

            String iterationSeries = iterations.stream().map(i -> Integer.toString(i.iteration())).reduce((a, b) -> a + "," + b).orElse("");
            String bestSeries = iterations.stream().map(i -> Double.toString(i.bestFitness())).reduce((a, b) -> a + "," + b).orElse("");
            String meanSeries = iterations.stream().map(i -> Double.toString(i.meanFitness())).reduce((a, b) -> a + "," + b).orElse("");
            String stdSeries = iterations.stream().map(i -> Double.toString(i.stdFitness())).reduce((a, b) -> a + "," + b).orElse("");

            String htmlTemplate = """
                    <!DOCTYPE html>
                    <html lang=\"en\">
                    <head>
                        <meta charset=\"UTF-8\" />
                        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
                        <title>EDAF v3 Report</title>
                        <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>
                        <style>
                            :root {
                                --bg: #f4f2eb;
                                --card: #fffdf7;
                                --ink: #1f2a32;
                                --accent: #1f7a8c;
                                --accent-2: #bf4342;
                                --line: #d9d5ca;
                            }
                            body { margin:0; font-family: 'IBM Plex Sans', 'Segoe UI', sans-serif; background: radial-gradient(circle at 15% 20%, #fff3d6, transparent 45%), radial-gradient(circle at 85% 10%, #d8f3f8, transparent 35%), var(--bg); color: var(--ink); }
                            .container { max-width: 1100px; margin: 0 auto; padding: 32px 20px 48px; }
                            .header { background: var(--card); border: 1px solid var(--line); border-radius: 14px; padding: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.06); }
                            .header h1 { margin: 0 0 8px; font-size: 1.6rem; }
                            .meta { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 10px; font-size: 0.95rem; }
                            .card { margin-top: 16px; background: var(--card); border: 1px solid var(--line); border-radius: 14px; padding: 20px; box-shadow: 0 8px 26px rgba(0,0,0,0.05); }
                            canvas { width: 100%; max-height: 320px; }
                            table { width: 100%; border-collapse: collapse; margin-top: 10px; }
                            th, td { border-bottom: 1px solid var(--line); padding: 8px; text-align: right; }
                            th:first-child, td:first-child { text-align: left; }
                        </style>
                    </head>
                    <body>
                        <div class=\"container\">
                            <section class=\"header\">
                                <h1>EDAF v3 Run Report</h1>
                                <div class=\"meta\">
                                    <div><strong>Run ID:</strong> {{RUN_ID}}</div>
                                    <div><strong>Algorithm:</strong> {{ALGORITHM}}</div>
                                    <div><strong>Model:</strong> {{MODEL}}</div>
                                    <div><strong>Problem:</strong> {{PROBLEM}}</div>
                                    <div><strong>Status:</strong> {{STATUS}}</div>
                                    <div><strong>Best Fitness:</strong> {{BEST_FITNESS}}</div>
                                    <div><strong>Runtime (ms):</strong> {{RUNTIME_MS}}</div>
                                </div>
                            </section>

                            <section class=\"card\">
                                <h2>Fitness Trajectory</h2>
                                <canvas id=\"fitnessChart\"></canvas>
                            </section>

                            <section class=\"card\">
                                <h2>Standard Deviation Trace</h2>
                                <canvas id=\"stdChart\"></canvas>
                            </section>

                            <section class=\"card\">
                                <h2>Last 20 Iterations</h2>
                                <table>
                                    <thead><tr><th>Iteration</th><th>Best</th><th>Mean</th><th>Std</th><th>Evaluations</th></tr></thead>
                                    <tbody>
                                        {{TABLE_ROWS}}
                                    </tbody>
                                </table>
                            </section>
                        </div>

                        <script>
                            const iterations = [{{ITERATION_SERIES}}];
                            const best = [{{BEST_SERIES}}];
                            const mean = [{{MEAN_SERIES}}];
                            const std = [{{STD_SERIES}}];

                            new Chart(document.getElementById('fitnessChart'), {
                                type: 'line',
                                data: {
                                    labels: iterations,
                                    datasets: [
                                        { label: 'Best', data: best, borderColor: '#1f7a8c', tension: 0.15 },
                                        { label: 'Mean', data: mean, borderColor: '#bf4342', tension: 0.15 }
                                    ]
                                },
                                options: { responsive: true, maintainAspectRatio: false }
                            });

                            new Chart(document.getElementById('stdChart'), {
                                type: 'line',
                                data: { labels: iterations, datasets: [{ label: 'Std', data: std, borderColor: '#7a6f9b', tension: 0.15 }] },
                                options: { responsive: true, maintainAspectRatio: false }
                            });
                        </script>
                    </body>
                    </html>
                    """;

            String html = htmlTemplate
                    .replace("{{RUN_ID}}", escapeHtml(run.runId()))
                    .replace("{{ALGORITHM}}", escapeHtml(run.algorithm()))
                    .replace("{{MODEL}}", escapeHtml(run.model()))
                    .replace("{{PROBLEM}}", escapeHtml(run.problem()))
                    .replace("{{STATUS}}", escapeHtml(run.status() == null ? "UNKNOWN" : run.status()))
                    .replace("{{BEST_FITNESS}}", formatNullableDouble(run.bestFitness()))
                    .replace("{{RUNTIME_MS}}", run.runtimeMillis() == null ? "n/a" : Long.toString(run.runtimeMillis()))
                    .replace("{{TABLE_ROWS}}", toTableRows(iterations))
                    .replace("{{ITERATION_SERIES}}", iterationSeries)
                    .replace("{{BEST_SERIES}}", bestSeries)
                    .replace("{{MEAN_SERIES}}", meanSeries)
                    .replace("{{STD_SERIES}}", stdSeries);

            Files.writeString(out, html);
            return out;
        } catch (IOException e) {
            throw new RuntimeException("Failed generating HTML report", e);
        }
    }

    @Override
    public String format() {
        return "html";
    }

    private static String toTableRows(List<IterationMetric> iterations) {
        int from = Math.max(0, iterations.size() - 20);
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < iterations.size(); i++) {
            IterationMetric row = iterations.get(i);
            sb.append("<tr><td>")
                    .append(row.iteration())
                    .append("</td><td>")
                    .append(String.format(Locale.ROOT, "%.6f", row.bestFitness()))
                    .append("</td><td>")
                    .append(String.format(Locale.ROOT, "%.6f", row.meanFitness()))
                    .append("</td><td>")
                    .append(String.format(Locale.ROOT, "%.6f", row.stdFitness()))
                    .append("</td><td>")
                    .append(row.evaluations())
                    .append("</td></tr>");
        }
        return sb.toString();
    }

    private static String formatNullableDouble(Double value) {
        if (value == null) {
            return "n/a";
        }
        return String.format(Locale.ROOT, "%.6f", value);
    }

    /**
     * Escapes user-derived strings before embedding them in HTML.
     */
    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
