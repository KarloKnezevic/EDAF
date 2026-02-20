package com.knezevic.edaf.v3.coco.report;

import com.knezevic.edaf.v3.coco.model.CocoAggregateRow;
import com.knezevic.edaf.v3.coco.model.CocoCampaignSnapshot;
import com.knezevic.edaf.v3.coco.model.CocoTrialRow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates standalone HTML report for one COCO campaign.
 */
public final class CocoHtmlReportGenerator {

    /**
     * Writes COCO campaign report and returns output path.
     */
    public Path generate(CocoCampaignSnapshot snapshot, Path outputDirectory) {
        try {
            Files.createDirectories(outputDirectory);
            Path out = outputDirectory.resolve("coco-campaign-" + snapshot.campaignId() + ".html");

            List<CocoAggregateRow> aggregates = snapshot.aggregates();
            int totalTrials = snapshot.trials().size();
            int successfulTrials = (int) snapshot.trials().stream().filter(CocoTrialRow::reachedTarget).count();
            double globalSuccess = totalTrials == 0 ? 0.0 : successfulTrials / (double) totalTrials;

            String aggregateRows = aggregates.stream()
                    .map(this::aggregateRowHtml)
                    .collect(Collectors.joining());

            String trialRows = snapshot.trials().stream()
                    .limit(800)
                    .map(this::trialRowHtml)
                    .collect(Collectors.joining());

            Map<String, List<CocoAggregateRow>> byOptimizer = aggregates.stream()
                    .collect(Collectors.groupingBy(CocoAggregateRow::optimizerId, LinkedHashMap::new, Collectors.toList()));

            String optimizerDatasets = buildRatioDatasets(byOptimizer);
            String dimensionLabels = buildDimensionLabels(aggregates);

            String html = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8" />
                        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                        <title>EDAF COCO Campaign Report</title>
                        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                        <style>
                            :root {
                                --bg: #f6faf9;
                                --card: #ffffff;
                                --ink: #15212b;
                                --muted: #5d6c7a;
                                --line: #d7e2e8;
                                --accent: #0f766e;
                                --accent2: #b45309;
                            }
                            * { box-sizing: border-box; }
                            body {
                                margin: 0;
                                font-family: "Segoe UI", "Inter", sans-serif;
                                color: var(--ink);
                                background:
                                    radial-gradient(circle at 12% 14%, rgba(15,118,110,.16), transparent 32%),
                                    radial-gradient(circle at 88% 8%, rgba(180,83,9,.14), transparent 28%),
                                    var(--bg);
                            }
                            .wrap { max-width: 1400px; margin: 0 auto; padding: 24px 16px 36px; }
                            .card {
                                background: var(--card);
                                border: 1px solid var(--line);
                                border-radius: 14px;
                                box-shadow: 0 10px 28px rgba(0,0,0,.06);
                                padding: 14px;
                                margin-bottom: 12px;
                            }
                            .head h1 { margin: 0; font-size: 1.6rem; }
                            .sub { margin-top: 5px; color: var(--muted); }
                            .grid {
                                display: grid;
                                grid-template-columns: repeat(6, minmax(140px, 1fr));
                                gap: 8px;
                            }
                            .kpi { border: 1px solid var(--line); border-radius: 12px; padding: 8px; }
                            .kpi .k { color: var(--muted); font-size: .78rem; }
                            .kpi .v { font-weight: 700; margin-top: 4px; }
                            .table-wrap { overflow: auto; border: 1px solid var(--line); border-radius: 10px; }
                            table { border-collapse: collapse; width: 100%; min-width: 980px; }
                            th, td { padding: 9px 10px; border-bottom: 1px solid var(--line); text-align: left; font-size: .85rem; }
                            th { color: var(--muted); background: #f8fbfd; }
                            .mono { font-family: "IBM Plex Mono", monospace; }
                            .canvas-wrap { height: 330px; }
                            .muted { color: var(--muted); font-size: .85rem; }
                        </style>
                    </head>
                    <body>
                    <div class="wrap">
                        <section class="card head">
                            <h1>EDAF COCO Campaign: {{CAMPAIGN_NAME}}</h1>
                            <div class="sub">campaign_id=<span class="mono">{{CAMPAIGN_ID}}</span> · suite={{SUITE}} · status={{STATUS}}</div>
                            <div class="sub">created={{CREATED_AT}} · started={{STARTED_AT}} · finished={{FINISHED_AT}}</div>
                            <div class="sub">generated={{GENERATED_AT}}</div>
                        </section>

                        <section class="card">
                            <div class="grid">
                                <div class="kpi"><div class="k">Optimizers</div><div class="v">{{OPTIMIZER_COUNT}}</div></div>
                                <div class="kpi"><div class="k">Trials</div><div class="v">{{TOTAL_TRIALS}}</div></div>
                                <div class="kpi"><div class="k">Reached Target</div><div class="v">{{SUCCESSFUL_TRIALS}}</div></div>
                                <div class="kpi"><div class="k">Global Success Rate</div><div class="v">{{GLOBAL_SUCCESS}}</div></div>
                                <div class="kpi"><div class="k">Functions</div><div class="v mono">{{FUNCTIONS}}</div></div>
                                <div class="kpi"><div class="k">Dimensions</div><div class="v mono">{{DIMENSIONS}}</div></div>
                            </div>
                        </section>

                        <section class="card">
                            <h2>ERT Ratio vs Reference (lower is better)</h2>
                            <p class="muted">Ratio = EDAF ERT / reference ERT. If reference is missing for a slice, point is omitted.</p>
                            <div class="canvas-wrap"><canvas id="ratioChart"></canvas></div>
                        </section>

                        <section class="card">
                            <h2>Aggregate Table</h2>
                            <div class="table-wrap">
                                <table>
                                    <thead>
                                    <tr>
                                        <th>Optimizer</th>
                                        <th>Dimension</th>
                                        <th>Target</th>
                                        <th>Success Rate</th>
                                        <th>Mean Evals to Target</th>
                                        <th>EDAF ERT</th>
                                        <th>Reference</th>
                                        <th>Reference ERT</th>
                                        <th>ERT Ratio</th>
                                        <th>Median Best</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                        {{AGGREGATE_ROWS}}
                                    </tbody>
                                </table>
                            </div>
                        </section>

                        <section class="card">
                            <h2>Trial Table (first 800 rows)</h2>
                            <div class="table-wrap">
                                <table>
                                    <thead>
                                    <tr>
                                        <th>Optimizer</th>
                                        <th>Run ID</th>
                                        <th>f</th>
                                        <th>instance</th>
                                        <th>dim</th>
                                        <th>rep</th>
                                        <th>budget</th>
                                        <th>evals</th>
                                        <th>best</th>
                                        <th>reached</th>
                                        <th>evals_to_target</th>
                                        <th>status</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                        {{TRIAL_ROWS}}
                                    </tbody>
                                </table>
                            </div>
                        </section>
                    </div>

                    <script>
                        const dimensionLabels = [{{DIMENSION_LABELS}}];
                        const ratioDatasets = [{{RATIO_DATASETS}}];
                        new Chart(document.getElementById('ratioChart'), {
                            type: 'line',
                            data: { labels: dimensionLabels, datasets: ratioDatasets },
                            options: {
                                responsive: true,
                                maintainAspectRatio: false,
                                interaction: { mode: 'index', intersect: false },
                                scales: {
                                    y: { title: { display: true, text: 'ERT ratio (EDAF/reference)' }, beginAtZero: true }
                                }
                            }
                        });
                    </script>
                    </body>
                    </html>
                    """;

            html = html.replace("{{CAMPAIGN_NAME}}", esc(snapshot.name()))
                    .replace("{{CAMPAIGN_ID}}", esc(snapshot.campaignId()))
                    .replace("{{SUITE}}", esc(snapshot.suite()))
                    .replace("{{STATUS}}", esc(snapshot.status()))
                    .replace("{{CREATED_AT}}", esc(nullToDash(snapshot.createdAt())))
                    .replace("{{STARTED_AT}}", esc(nullToDash(snapshot.startedAt())))
                    .replace("{{FINISHED_AT}}", esc(nullToDash(snapshot.finishedAt())))
                    .replace("{{GENERATED_AT}}", esc(Instant.now().toString()))
                    .replace("{{OPTIMIZER_COUNT}}", Integer.toString(snapshot.optimizers().size()))
                    .replace("{{TOTAL_TRIALS}}", Integer.toString(totalTrials))
                    .replace("{{SUCCESSFUL_TRIALS}}", Integer.toString(successfulTrials))
                    .replace("{{GLOBAL_SUCCESS}}", fmtPercent(globalSuccess))
                    .replace("{{FUNCTIONS}}", esc(nullToDash(snapshot.functionsJson())))
                    .replace("{{DIMENSIONS}}", esc(nullToDash(snapshot.dimensionsJson())))
                    .replace("{{AGGREGATE_ROWS}}", aggregateRows)
                    .replace("{{TRIAL_ROWS}}", trialRows)
                    .replace("{{DIMENSION_LABELS}}", dimensionLabels)
                    .replace("{{RATIO_DATASETS}}", optimizerDatasets);

            Files.writeString(out, html);
            return out;
        } catch (IOException e) {
            throw new RuntimeException("Failed generating COCO HTML report", e);
        }
    }

    private String aggregateRowHtml(CocoAggregateRow row) {
        return "<tr>"
                + "<td class='mono'>" + esc(row.optimizerId()) + "</td>"
                + "<td>" + row.dimension() + "</td>"
                + "<td class='mono'>" + fmtDouble(row.targetValue()) + "</td>"
                + "<td>" + fmtPercent(row.successRate()) + "</td>"
                + "<td>" + fmtNullable(row.meanEvaluationsToTarget()) + "</td>"
                + "<td>" + fmtNullable(row.edafErt()) + "</td>"
                + "<td class='mono'>" + esc(nullToDash(row.comparedReferenceOptimizer())) + "</td>"
                + "<td>" + fmtNullable(row.referenceErt()) + "</td>"
                + "<td>" + fmtNullable(row.ertRatio()) + "</td>"
                + "<td>" + fmtNullable(row.medianBestFitness()) + "</td>"
                + "</tr>";
    }

    private String trialRowHtml(CocoTrialRow row) {
        return "<tr>"
                + "<td class='mono'>" + esc(row.optimizerId()) + "</td>"
                + "<td class='mono'>" + esc(row.runId()) + "</td>"
                + "<td>f" + row.functionId() + "</td>"
                + "<td>" + row.instanceId() + "</td>"
                + "<td>" + row.dimension() + "</td>"
                + "<td>" + row.repetition() + "</td>"
                + "<td>" + row.budgetEvaluations() + "</td>"
                + "<td>" + nullToDash(row.evaluations()) + "</td>"
                + "<td>" + fmtNullable(row.bestFitness()) + "</td>"
                + "<td>" + (row.reachedTarget() ? "yes" : "no") + "</td>"
                + "<td>" + nullToDash(row.evaluationsToTarget()) + "</td>"
                + "<td>" + esc(row.status()) + "</td>"
                + "</tr>";
    }

    private static String buildDimensionLabels(List<CocoAggregateRow> rows) {
        List<Integer> dimensions = rows.stream().map(CocoAggregateRow::dimension).distinct().sorted().toList();
        return dimensions.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private static String buildRatioDatasets(Map<String, List<CocoAggregateRow>> byOptimizer) {
        String[] colors = {"#0f766e", "#0b4a6f", "#b45309", "#9333ea", "#be123c", "#1d4ed8", "#475569"};
        List<Integer> dimensions = byOptimizer.values().stream()
                .flatMap(List::stream)
                .map(CocoAggregateRow::dimension)
                .distinct()
                .sorted()
                .toList();

        List<String> datasets = new ArrayList<>();
        int colorIndex = 0;
        for (Map.Entry<String, List<CocoAggregateRow>> entry : byOptimizer.entrySet()) {
            Map<Integer, CocoAggregateRow> byDimension = entry.getValue().stream()
                    .collect(Collectors.toMap(CocoAggregateRow::dimension, r -> r, (a, b) -> b, LinkedHashMap::new));

            String data = dimensions.stream()
                    .map(dim -> {
                        CocoAggregateRow row = byDimension.get(dim);
                        if (row == null || row.ertRatio() == null || row.ertRatio().isNaN()) {
                            return "null";
                        }
                        return fmtDouble(row.ertRatio());
                    })
                    .collect(Collectors.joining(","));

            String color = colors[colorIndex % colors.length];
            colorIndex++;
            datasets.add("{label:'" + js(entry.getKey()) + "',data:[" + data + "],borderColor:'" + color
                    + "',backgroundColor:'" + color + "33',tension:0.2}");
        }
        return String.join(",", datasets);
    }

    private static String fmtPercent(double value) {
        return String.format(Locale.ROOT, "%.1f%%", value * 100.0);
    }

    private static String fmtNullable(Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return "-";
        }
        return fmtDouble(value);
    }

    private static String fmtDouble(double value) {
        return String.format(Locale.ROOT, "%.6g", value);
    }

    private static String nullToDash(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String js(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
