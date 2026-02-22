package com.knezevic.edaf.v3.reporting;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knezevic.edaf.v3.persistence.query.IterationMetric;
import com.knezevic.edaf.v3.persistence.query.RunSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generates standalone HTML report with latent-knowledge visualizations.
 */
public final class HtmlReportGenerator implements ReportGenerator {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Path generate(RunSummary run, List<IterationMetric> iterations, Path outputDir) {
        try {
            Files.createDirectories(outputDir);
            Path out = outputDir.resolve("report-" + run.runId() + ".html");

            ParsedSeries parsed = parseSeries(iterations);
            String tableRows = toTableRows(iterations);
            String highlights = escapeHtml(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed.highlights()));

            String html = """
                    <!doctype html>
                    <html lang=\"en\">
                    <head>
                        <meta charset=\"utf-8\" />
                        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />
                        <title>EDAF Run Report</title>
                        <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>
                        <style>
                            :root { --bg:#f4f8fb; --card:#fff; --line:#d6dfeb; --ink:#1b2734; --muted:#607080; --accent:#0f766e; }
                            body { margin:0; font-family: 'Space Grotesk','Segoe UI',sans-serif; color:var(--ink); background:var(--bg); }
                            .wrap { max-width: 1160px; margin: 0 auto; padding: 24px; }
                            .card { background: var(--card); border:1px solid var(--line); border-radius:12px; padding:16px; margin-bottom:12px; }
                            .grid { display:grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap:10px; }
                            .k { color:var(--muted); font-size:.82rem; font-family:'IBM Plex Mono',monospace; }
                            .v { margin-top:5px; font-weight:700; }
                            .row { display:grid; grid-template-columns: 1fr 1fr; gap:12px; }
                            .chart { height:300px; }
                            table { width:100%; border-collapse: collapse; }
                            th, td { border-bottom:1px solid var(--line); padding:8px; font-size:.84rem; text-align:left; }
                            th { color:var(--muted); font-family:'IBM Plex Mono',monospace; }
                            pre { margin:0; white-space:pre-wrap; overflow-wrap:anywhere; max-height:320px; overflow:auto; background:#f8fbff; border:1px solid var(--line); border-radius:10px; padding:10px; font-size:.78rem; }
                            @media (max-width: 920px) { .row { grid-template-columns: 1fr; } }
                        </style>
                    </head>
                    <body>
                    <div class=\"wrap\">
                        <section class=\"card\">
                            <h1>EDAF Run Report</h1>
                            <div class=\"grid\">
                                <div><div class=\"k\">Run ID</div><div class=\"v\">{{RUN_ID}}</div></div>
                                <div><div class=\"k\">Algorithm</div><div class=\"v\">{{ALGORITHM}}</div></div>
                                <div><div class=\"k\">Model</div><div class=\"v\">{{MODEL}}</div></div>
                                <div><div class=\"k\">Problem</div><div class=\"v\">{{PROBLEM}}</div></div>
                                <div><div class=\"k\">Status</div><div class=\"v\">{{STATUS}}</div></div>
                                <div><div class=\"k\">Best Fitness</div><div class=\"v\">{{BEST_FITNESS}}</div></div>
                                <div><div class=\"k\">Runtime (ms)</div><div class=\"v\">{{RUNTIME_MS}}</div></div>
                                <div><div class=\"k\">Representation</div><div class=\"v\">{{REPRESENTATION}}</div></div>
                            </div>
                        </section>

                        <section class=\"card\">
                            <h2>Fitness</h2>
                            <div class=\"chart\"><canvas id=\"fitnessChart\"></canvas></div>
                        </section>

                        <section class=\"card row\">
                            <div>
                                <h2>Diversity</h2>
                                <div class=\"chart\"><canvas id=\"diversityChart\"></canvas></div>
                            </div>
                            <div>
                                <h2>Drift</h2>
                                <div class=\"chart\"><canvas id=\"driftChart\"></canvas></div>
                            </div>
                        </section>

                        <section class=\"card\">
                            <h2>Latent Knowledge Highlights</h2>
                            <pre>{{HIGHLIGHTS}}</pre>
                        </section>

                        <section class=\"card\">
                            <h2>Last 20 Iterations</h2>
                            <table>
                                <thead><tr><th>Iteration</th><th>Best</th><th>Mean</th><th>Std</th><th>Evaluations</th></tr></thead>
                                <tbody>{{TABLE_ROWS}}</tbody>
                            </table>
                        </section>
                    </div>

                    <script>
                        const iterations = [{{ITERATIONS}}];
                        const best = [{{BEST_SERIES}}];
                        const mean = [{{MEAN_SERIES}}];
                        const std = [{{STD_SERIES}}];
                        const diversity = [{{DIVERSITY_SERIES}}];
                        const drift = [{{DRIFT_SERIES}}];

                        new Chart(document.getElementById('fitnessChart'), {
                            type: 'line',
                            data: {
                                labels: iterations,
                                datasets: [
                                    { label: 'Best', data: best, borderColor: '#0f766e', tension: 0.2 },
                                    { label: 'Mean', data: mean, borderColor: '#1d4ed8', tension: 0.2 },
                                    { label: 'Std', data: std, borderColor: '#b45309', tension: 0.2 }
                                ]
                            },
                            options: { responsive: true, maintainAspectRatio: false }
                        });

                        new Chart(document.getElementById('diversityChart'), {
                            type: 'line',
                            data: {
                                labels: iterations,
                                datasets: [{ label: 'Diversity signal', data: diversity, borderColor: '#0f766e', tension: 0.2 }]
                            },
                            options: { responsive: true, maintainAspectRatio: false }
                        });

                        new Chart(document.getElementById('driftChart'), {
                            type: 'line',
                            data: {
                                labels: iterations,
                                datasets: [{ label: 'Drift signal', data: drift, borderColor: '#9333ea', tension: 0.2 }]
                            },
                            options: { responsive: true, maintainAspectRatio: false }
                        });
                    </script>
                    </body>
                    </html>
                    """;

            html = html
                    .replace("{{RUN_ID}}", escapeHtml(run.runId()))
                    .replace("{{ALGORITHM}}", escapeHtml(run.algorithm()))
                    .replace("{{MODEL}}", escapeHtml(run.model()))
                    .replace("{{PROBLEM}}", escapeHtml(run.problem()))
                    .replace("{{STATUS}}", escapeHtml(run.status() == null ? "UNKNOWN" : run.status()))
                    .replace("{{BEST_FITNESS}}", formatNullableDouble(run.bestFitness()))
                    .replace("{{RUNTIME_MS}}", run.runtimeMillis() == null ? "n/a" : Long.toString(run.runtimeMillis()))
                    .replace("{{REPRESENTATION}}", escapeHtml(parsed.representationFamily()))
                    .replace("{{HIGHLIGHTS}}", highlights)
                    .replace("{{TABLE_ROWS}}", tableRows)
                    .replace("{{ITERATIONS}}", parsed.iterationSeries())
                    .replace("{{BEST_SERIES}}", parsed.bestSeries())
                    .replace("{{MEAN_SERIES}}", parsed.meanSeries())
                    .replace("{{STD_SERIES}}", parsed.stdSeries())
                    .replace("{{DIVERSITY_SERIES}}", parsed.diversitySeries())
                    .replace("{{DRIFT_SERIES}}", parsed.driftSeries());

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

    private ParsedSeries parseSeries(List<IterationMetric> rows) {
        List<Integer> iterations = new ArrayList<>();
        List<Double> best = new ArrayList<>();
        List<Double> mean = new ArrayList<>();
        List<Double> std = new ArrayList<>();
        List<Double> diversity = new ArrayList<>();
        List<Double> drift = new ArrayList<>();

        Map<String, Object> latestHighlights = Map.of();
        String family = "unknown";

        for (IterationMetric row : rows) {
            iterations.add(row.iteration());
            best.add(row.bestFitness());
            mean.add(row.meanFitness());
            std.add(row.stdFitness());

            Map<String, Object> diagnostics = parseMap(row.diagnosticsJson());
            Map<String, Object> latent = castMap(diagnostics.get("latentTelemetry"));
            Map<String, Object> diversityMap = castMap(latent.get("diversity"));
            Map<String, Object> driftMap = castMap(latent.get("drift"));
            Map<String, Object> insightsMap = castMap(latent.get("insights"));
            Map<String, Object> metricsMap = castMap(latent.get("metrics"));

            family = String.valueOf(latent.getOrDefault("representationFamily", family));

            diversity.add(diversitySignal(family, diversityMap));
            drift.add(driftSignal(family, driftMap));

            latestHighlights = new LinkedHashMap<>();
            latestHighlights.put("representationFamily", family);
            latestHighlights.put("metrics", metricsMap);
            latestHighlights.put("drift", driftMap);
            latestHighlights.put("diversity", diversityMap);

            if ("binary".equalsIgnoreCase(family)) {
                latestHighlights.put("mostFixedBits", insightsMap.getOrDefault("topDecidedBits", List.of()));
                latestHighlights.put("mostUncertainBits", insightsMap.getOrDefault("topUncertainBits", List.of()));
                latestHighlights.put("strongestDependencies", insightsMap.getOrDefault("dependencyEdges", List.of()));
            } else if ("permutation".equalsIgnoreCase(family)) {
                latestHighlights.put("strongestAdjacencyEdges", insightsMap.getOrDefault("topAdjacencyEdges", List.of()));
                latestHighlights.put("consensusPermutation", insightsMap.getOrDefault("consensusPermutation", List.of()));
            } else if ("real".equalsIgnoreCase(family)) {
                latestHighlights.put("smallestSigmaDims", insightsMap.getOrDefault("collapsedDimensions", List.of()));
                latestHighlights.put("mostVariableDims", insightsMap.getOrDefault("topVaryingDimensions", List.of()));
            }
        }

        return new ParsedSeries(
                csv(iterations),
                csv(best),
                csv(mean),
                csv(std),
                csv(diversity),
                csv(drift),
                family,
                latestHighlights
        );
    }

    private static double diversitySignal(String family, Map<String, Object> map) {
        if ("binary".equalsIgnoreCase(family)) {
            return asDouble(map.get("hamming_population"));
        }
        if ("permutation".equalsIgnoreCase(family)) {
            return asDouble(map.get("kendall_population"));
        }
        if ("real".equalsIgnoreCase(family)) {
            return asDouble(map.get("euclidean_population"));
        }
        return map.values().stream().findFirst().map(HtmlReportGenerator::asDouble).orElse(0.0);
    }

    private static double driftSignal(String family, Map<String, Object> map) {
        if ("binary".equalsIgnoreCase(family)) {
            return asDouble(map.get("binary_prob_l2"));
        }
        if ("permutation".equalsIgnoreCase(family)) {
            return asDouble(map.get("consensus_kendall"));
        }
        if ("real".equalsIgnoreCase(family)) {
            return asDouble(map.get("gaussian_kl_diag"));
        }
        return map.values().stream().findFirst().map(HtmlReportGenerator::asDouble).orElse(0.0);
    }

    private static String csv(List<?> values) {
        return values.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
    }

    private Map<String, Object> parseMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return mapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, Object> converted = new LinkedHashMap<>();
        map.forEach((key, nested) -> {
            if (key != null) {
                converted.put(String.valueOf(key), nested);
            }
        });
        return converted;
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

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private record ParsedSeries(
            String iterationSeries,
            String bestSeries,
            String meanSeries,
            String stdSeries,
            String diversitySeries,
            String driftSeries,
            String representationFamily,
            Map<String, Object> highlights
    ) {
    }
}
