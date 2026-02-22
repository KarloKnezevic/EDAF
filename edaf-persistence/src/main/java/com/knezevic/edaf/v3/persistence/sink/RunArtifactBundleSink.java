package com.knezevic.edaf.v3.persistence.sink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.knezevic.edaf.v3.core.api.AdaptiveActionRecord;
import com.knezevic.edaf.v3.core.api.LatentTelemetry;
import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.core.events.IterationCompletedEvent;
import com.knezevic.edaf.v3.core.events.RunCompletedEvent;
import com.knezevic.edaf.v3.core.events.RunEvent;
import com.knezevic.edaf.v3.core.events.RunFailedEvent;
import com.knezevic.edaf.v3.core.events.RunStartedEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Writes a self-contained run artifact bundle used by offline reporting and web fallback loading.
 */
public final class RunArtifactBundleSink implements EventSink {

    private final Path runDirectory;
    private final Path telemetryJsonl;
    private final Path eventsJsonl;
    private final Path metricsCsv;
    private final Path summaryJson;
    private final Path reportHtml;
    private final Path configYaml;
    private final Path configJson;

    private final String resolvedYaml;
    private final String resolvedJson;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final List<IterationSnapshot> iterations = new ArrayList<>();
    private final List<Map<String, Object>> adaptiveTimeline = new ArrayList<>();
    private final List<Map<String, Object>> eventTimeline = new ArrayList<>();

    private RunStartedEvent startedEvent;
    private RunCompletedEvent completedEvent;
    private RunFailedEvent failedEvent;
    private boolean csvHeaderWritten;

    public RunArtifactBundleSink(Path outputDirectory,
                                 String runId,
                                 String resolvedYaml,
                                 String resolvedJson) {
        this.runDirectory = outputDirectory.resolve("runs").resolve(runId);
        this.telemetryJsonl = runDirectory.resolve("telemetry.jsonl");
        this.eventsJsonl = runDirectory.resolve("events.jsonl");
        this.metricsCsv = runDirectory.resolve("metrics.csv");
        this.summaryJson = runDirectory.resolve("summary.json");
        this.reportHtml = runDirectory.resolve("report.html");
        this.configYaml = runDirectory.resolve("config-resolved.yaml");
        this.configJson = runDirectory.resolve("config-resolved.json");
        this.resolvedYaml = resolvedYaml;
        this.resolvedJson = resolvedJson;
    }

    @Override
    public synchronized void onEvent(RunEvent event) {
        ensureInitialized();
        appendEventTimeline(event);

        if (event instanceof RunStartedEvent started) {
            this.startedEvent = started;
            return;
        }

        if (event instanceof IterationCompletedEvent iteration) {
            appendIteration(iteration);
            return;
        }

        if (event instanceof RunCompletedEvent completed) {
            this.completedEvent = completed;
            writeSummaryAndReport();
            return;
        }

        if (event instanceof RunFailedEvent failed) {
            this.failedEvent = failed;
            writeSummaryAndReport();
        }
    }

    public Path runDirectory() {
        return runDirectory;
    }

    public Path telemetryJsonl() {
        return telemetryJsonl;
    }

    public Path metricsCsv() {
        return metricsCsv;
    }

    public Path eventsJsonl() {
        return eventsJsonl;
    }

    public Path summaryJson() {
        return summaryJson;
    }

    public Path reportHtml() {
        return reportHtml;
    }

    private void ensureInitialized() {
        try {
            Files.createDirectories(runDirectory);
            if (!Files.exists(configYaml)) {
                Files.writeString(configYaml, resolvedYaml, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            if (!Files.exists(configJson)) {
                Files.writeString(configJson, resolvedJson, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed initializing run artifact bundle", e);
        }
    }

    private void appendEventTimeline(RunEvent event) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("timestamp", event.timestamp().toString());
        row.put("type", event.type());
        row.put("runId", event.runId());
        row.put("payload", mapper.convertValue(event, Object.class));
        eventTimeline.add(row);
        try {
            Files.writeString(
                    eventsJsonl,
                    mapper.writeValueAsString(row) + "\n",
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed appending events JSONL", e);
        }
    }

    private void appendIteration(IterationCompletedEvent iteration) {
        String representationFamily = iteration.latentTelemetry() == null
                ? "unknown"
                : iteration.latentTelemetry().representationFamily();

        double diversitySignal = diversitySignal(representationFamily, iteration.latentTelemetry());
        double driftSignal = driftSignal(representationFamily, iteration.latentTelemetry());

        IterationSnapshot snapshot = new IterationSnapshot(
                iteration.timestamp().toString(),
                iteration.iteration(),
                iteration.evaluations(),
                iteration.populationSize(),
                iteration.eliteSize(),
                iteration.bestFitness(),
                iteration.meanFitness(),
                iteration.stdFitness(),
                representationFamily,
                diversitySignal,
                driftSignal,
                iteration.metrics(),
                iteration.latentTelemetry(),
                iteration.adaptiveActions()
        );
        iterations.add(snapshot);

        for (AdaptiveActionRecord action : iteration.adaptiveActions()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("timestamp", iteration.timestamp().toString());
            row.put("iteration", iteration.iteration());
            row.put("trigger", action.trigger());
            row.put("actionType", action.actionType());
            row.put("reason", action.reason());
            row.put("details", action.details());
            adaptiveTimeline.add(row);
        }

        Map<String, Object> telemetryRow = new LinkedHashMap<>();
        telemetryRow.put("timestamp", iteration.timestamp().toString());
        telemetryRow.put("runId", iteration.runId());
        telemetryRow.put("generation", iteration.iteration());
        telemetryRow.put("evaluations", iteration.evaluations());
        telemetryRow.put("populationSize", iteration.populationSize());
        telemetryRow.put("eliteSize", iteration.eliteSize());
        telemetryRow.put("bestFitness", iteration.bestFitness());
        telemetryRow.put("meanFitness", iteration.meanFitness());
        telemetryRow.put("stdFitness", iteration.stdFitness());
        telemetryRow.put("representationFamily", representationFamily);
        telemetryRow.put("algorithm", startedEvent == null ? null : startedEvent.algorithm());
        telemetryRow.put("model", startedEvent == null ? null : startedEvent.model());
        telemetryRow.put("problem", startedEvent == null ? null : startedEvent.problem());
        telemetryRow.put("metrics", iteration.metrics());
        telemetryRow.put("latentTelemetry", iteration.latentTelemetry());
        telemetryRow.put("adaptiveActions", iteration.adaptiveActions());

        try {
            Files.writeString(
                    telemetryJsonl,
                    mapper.writeValueAsString(telemetryRow) + "\n",
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed appending telemetry JSONL", e);
        }

        try {
            if (!csvHeaderWritten) {
                Files.writeString(metricsCsv,
                        "timestamp,run_id,generation,evaluations,population_size,elite_size,best_fitness,mean_fitness,std_fitness,diversity_signal,drift_signal,adaptive_event_count\n",
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                csvHeaderWritten = true;
            }
            String line = String.format(Locale.ROOT,
                    "\"%s\",\"%s\",%d,%d,%d,%d,%.12f,%.12f,%.12f,%.12f,%.12f,%d\n",
                    iteration.timestamp(),
                    iteration.runId(),
                    iteration.iteration(),
                    iteration.evaluations(),
                    iteration.populationSize(),
                    iteration.eliteSize(),
                    iteration.bestFitness(),
                    iteration.meanFitness(),
                    iteration.stdFitness(),
                    diversitySignal,
                    driftSignal,
                    iteration.adaptiveActions().size());
            Files.writeString(metricsCsv, line, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed appending compact metrics CSV", e);
        }
    }

    private void writeSummaryAndReport() {
        try {
            Map<String, Object> summary = buildSummary();
            Files.writeString(summaryJson,
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(summary),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(reportHtml,
                    buildHtmlReport(summary),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed writing run artifact summary/report", e);
        }
    }

    private Map<String, Object> buildSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        String status = failedEvent != null ? "FAILED" : (completedEvent != null ? "COMPLETED" : "RUNNING");

        summary.put("runId", startedEvent == null
                ? (completedEvent != null ? completedEvent.runId() : "unknown")
                : startedEvent.runId());
        summary.put("status", status);
        summary.put("algorithm", startedEvent == null ? null : startedEvent.algorithm());
        summary.put("model", startedEvent == null ? null : startedEvent.model());
        summary.put("problem", startedEvent == null ? null : startedEvent.problem());
        summary.put("masterSeed", startedEvent == null ? null : startedEvent.masterSeed());
        summary.put("startedAt", startedEvent == null ? null : startedEvent.timestamp().toString());
        summary.put("endedAt", completedEvent != null ? completedEvent.timestamp().toString()
                : (failedEvent != null ? failedEvent.timestamp().toString() : null));
        summary.put("runtimeMillis", completedEvent == null ? null : completedEvent.runtimeMillis());
        summary.put("iterations", completedEvent == null ? iterations.size() : completedEvent.iterations());
        summary.put("evaluations", completedEvent == null
                ? (iterations.isEmpty() ? null : iterations.get(iterations.size() - 1).evaluations())
                : completedEvent.evaluations());
        summary.put("bestFitness", completedEvent == null
                ? (iterations.isEmpty() ? null : iterations.get(iterations.size() - 1).bestFitness())
                : completedEvent.bestFitness());
        summary.put("bestSummary", completedEvent == null ? null : completedEvent.bestSummary());
        summary.put("errorMessage", failedEvent == null ? null : failedEvent.errorMessage());

        summary.put("artifactPaths", Map.of(
                "runDirectory", runDirectory.toString(),
                "telemetryJsonl", telemetryJsonl.toString(),
                "eventsJsonl", eventsJsonl.toString(),
                "metricsCsv", metricsCsv.toString(),
                "summaryJson", summaryJson.toString(),
                "reportHtml", reportHtml.toString(),
                "configYaml", configYaml.toString(),
                "configJson", configJson.toString()
        ));

        summary.put("adaptiveEvents", adaptiveTimeline);
        summary.put("eventCount", eventTimeline.size());
        summary.put("adaptiveEventCount", adaptiveTimeline.size());

        if (!iterations.isEmpty()) {
            IterationSnapshot latest = iterations.get(iterations.size() - 1);
            summary.put("latestRepresentationFamily", latest.representationFamily());
            summary.put("latentHighlights", latentHighlights(latest.latentTelemetry()));
        } else {
            summary.put("latestRepresentationFamily", "unknown");
            summary.put("latentHighlights", Map.of());
        }

        summary.put("generatedAt", Instant.now().toString());
        return summary;
    }

    private Map<String, Object> latentHighlights(LatentTelemetry telemetry) {
        if (telemetry == null) {
            return Map.of();
        }
        Map<String, Object> highlights = new LinkedHashMap<>();
        highlights.put("representationFamily", telemetry.representationFamily());
        highlights.put("metrics", telemetry.metrics());
        highlights.put("drift", telemetry.drift());
        highlights.put("diversity", telemetry.diversity());

        if ("binary".equalsIgnoreCase(telemetry.representationFamily())) {
            highlights.put("mostFixedBits", telemetry.insights().getOrDefault("topDecidedBits", List.of()));
            highlights.put("mostUncertainBits", telemetry.insights().getOrDefault("topUncertainBits", List.of()));
            highlights.put("strongestDependencies", telemetry.insights().getOrDefault("dependencyEdges", List.of()));
        } else if ("permutation".equalsIgnoreCase(telemetry.representationFamily())) {
            highlights.put("strongestAdjacencyEdges", telemetry.insights().getOrDefault("topAdjacencyEdges", List.of()));
            highlights.put("consensusPermutation", telemetry.insights().getOrDefault("consensusPermutation", List.of()));
            highlights.put("positionEntropy", telemetry.insights().getOrDefault("positionEntropyPerItem", List.of()));
        } else if ("real".equalsIgnoreCase(telemetry.representationFamily())) {
            highlights.put("smallestSigmaDims", telemetry.insights().getOrDefault("collapsedDimensions", List.of()));
            highlights.put("mostVariableDims", telemetry.insights().getOrDefault("topVaryingDimensions", List.of()));
            highlights.put("driftPeaks", telemetry.drift());
        }

        return highlights;
    }

    private String buildHtmlReport(Map<String, Object> summary) throws IOException {
        String iterationSeries = iterations.stream()
                .map(row -> Integer.toString(row.iteration()))
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        String bestSeries = iterations.stream()
                .map(row -> Double.toString(row.bestFitness()))
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        String meanSeries = iterations.stream()
                .map(row -> Double.toString(row.meanFitness()))
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        String stdSeries = iterations.stream()
                .map(row -> Double.toString(row.stdFitness()))
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        String diversitySeries = iterations.stream()
                .map(row -> Double.toString(row.diversitySignal()))
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        String driftSeries = iterations.stream()
                .map(row -> Double.toString(row.driftSignal()))
                .reduce((left, right) -> left + "," + right)
                .orElse("");

        IterationSnapshot latest = iterations.isEmpty() ? null : iterations.get(iterations.size() - 1);
        String highlightsJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(summary.get("latentHighlights"));
        String eventsRows = adaptiveTimeline.isEmpty()
                ? "<tr><td colspan=\"5\">No adaptive events were triggered.</td></tr>"
                : adaptiveTimeline.stream().map(row -> "<tr>"
                        + "<td>" + escapeHtml(String.valueOf(row.get("timestamp"))) + "</td>"
                        + "<td>" + escapeHtml(String.valueOf(row.get("iteration"))) + "</td>"
                        + "<td>" + escapeHtml(String.valueOf(row.get("trigger"))) + "</td>"
                        + "<td>" + escapeHtml(String.valueOf(row.get("actionType"))) + "</td>"
                        + "<td>" + escapeHtml(String.valueOf(row.get("reason"))) + "</td>"
                        + "</tr>")
                .reduce((left, right) -> left + right)
                .orElse("");

        String representation = latest == null ? "unknown" : latest.representationFamily();
        String representationInsightsSection = representationInsightsSection(representation);
        String latentSeriesJson = mapper.writeValueAsString(
                iterations.stream().map(IterationSnapshot::latentTelemetry).toList()
        );

        return """
                <!doctype html>
                <html lang=\"en\">
                <head>
                    <meta charset=\"utf-8\" />
                    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />
                    <title>EDAF Run Artifact Report</title>
                    <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>
                    <style>
                        :root { --bg:#f5f8fb; --card:#ffffff; --line:#d5ddea; --ink:#17212b; --muted:#5b6677; --accent:#0f766e; }
                        body { margin:0; font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, sans-serif; background:var(--bg); color:var(--ink); }
                        .wrap { max-width: 1180px; margin: 0 auto; padding: 24px; }
                        .card { background:var(--card); border:1px solid var(--line); border-radius:12px; padding:16px; margin-bottom:14px; }
                        .grid { display:grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap:10px; }
                        .k { color:var(--muted); font-size:.82rem; }
                        .v { font-weight:700; margin-top:5px; word-break:break-word; }
                        .row { display:grid; grid-template-columns: 1fr 1fr; gap:12px; }
                        .chart { height:280px; }
                        table { width:100%%; border-collapse: collapse; }
                        th, td { border-bottom:1px solid var(--line); padding:8px; text-align:left; font-size:.85rem; }
                        th { color:var(--muted); }
                        pre { margin:0; white-space:pre-wrap; overflow-wrap:anywhere; max-height:320px; overflow:auto; background:#f8fbff; border:1px solid var(--line); border-radius:10px; padding:10px; }
                        @media (max-width: 920px) { .row { grid-template-columns: 1fr; } }
                    </style>
                </head>
                <body>
                <div class=\"wrap\">
                    <section class=\"card\">
                        <h1>EDAF Run Report</h1>
                        <div class=\"grid\">
                            <div><div class=\"k\">Run ID</div><div class=\"v\">%s</div></div>
                            <div><div class=\"k\">Status</div><div class=\"v\">%s</div></div>
                            <div><div class=\"k\">Algorithm</div><div class=\"v\">%s</div></div>
                            <div><div class=\"k\">Model</div><div class=\"v\">%s</div></div>
                            <div><div class=\"k\">Problem</div><div class=\"v\">%s</div></div>
                            <div><div class=\"k\">Best Fitness</div><div class=\"v\">%s</div></div>
                            <div><div class=\"k\">Iterations</div><div class=\"v\">%s</div></div>
                            <div><div class=\"k\">Evaluations</div><div class=\"v\">%s</div></div>
                            <div><div class=\"k\">Runtime (ms)</div><div class=\"v\">%s</div></div>
                            <div><div class=\"k\">Representation</div><div class=\"v\">%s</div></div>
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
                        <pre>%s</pre>
                    </section>

                    <section class=\"card\">
                        <h2>Adaptive Events Timeline</h2>
                        <table>
                            <thead><tr><th>Timestamp</th><th>Iteration</th><th>Trigger</th><th>Action</th><th>Reason</th></tr></thead>
                            <tbody>%s</tbody>
                        </table>
                    </section>

                    %s
                </div>

                <script>
                const iterations = [%s];
                const best = [%s];
                const mean = [%s];
                const std = [%s];
                const diversity = [%s];
                const drift = [%s];
                const latentSeries = %s;
                const representation = %s;

                new Chart(document.getElementById('fitnessChart'), {
                    type: 'line',
                    data: { labels: iterations, datasets: [
                        { label: 'Best', data: best, borderColor: '#0f766e', tension: 0.2 },
                        { label: 'Mean', data: mean, borderColor: '#1d4ed8', tension: 0.2 },
                        { label: 'Std', data: std, borderColor: '#b45309', tension: 0.2 }
                    ] },
                    options: { responsive: true, maintainAspectRatio: false }
                });

                new Chart(document.getElementById('diversityChart'), {
                    type: 'line',
                    data: { labels: iterations, datasets: [{ label: 'Diversity signal', data: diversity, borderColor: '#0f766e', tension: 0.2 }] },
                    options: { responsive: true, maintainAspectRatio: false }
                });

                new Chart(document.getElementById('driftChart'), {
                    type: 'line',
                    data: { labels: iterations, datasets: [{ label: 'Drift signal', data: drift, borderColor: '#9333ea', tension: 0.2 }] },
                    options: { responsive: true, maintainAspectRatio: false }
                });

                function palette(index) {
                    const colors = ['#0f766e', '#1d4ed8', '#9333ea', '#b45309', '#be123c', '#0e7490', '#15803d', '#6d28d9'];
                    return colors[index %% colors.length];
                }

                function heatColor(t) {
                    const x = Math.max(0, Math.min(1, t));
                    const r = Math.round(30 + 180 * x);
                    const g = Math.round(80 + 120 * (1 - Math.abs(x - 0.5) * 2));
                    const b = Math.round(180 - 120 * x);
                    return `rgb(${r},${g},${b})`;
                }

                function transpose(rows) {
                    if (!rows || !rows.length) return [];
                    const maxLen = rows.reduce((m, row) => Math.max(m, row.length), 0);
                    const out = Array.from({ length: maxLen }, () => Array(rows.length).fill(0));
                    for (let r = 0; r < rows.length; r++) {
                        for (let c = 0; c < maxLen; c++) {
                            out[c][r] = Number(rows[r][c] || 0);
                        }
                    }
                    return out;
                }

                function topChangingIndexes(series, topK) {
                    if (!series || !series.length) return [];
                    const length = series.reduce((m, row) => Math.max(m, row.length), 0);
                    const scores = [];
                    for (let idx = 0; idx < length; idx++) {
                        const values = series.map(row => Number(row[idx] || 0));
                        const min = Math.min(...values);
                        const max = Math.max(...values);
                        scores.push({ idx, range: max - min });
                    }
                    scores.sort((a, b) => b.range - a.range);
                    return scores.slice(0, Math.min(topK, scores.length)).map(s => s.idx);
                }

                function drawHeatmap(canvas, matrix, xLabel, yLabel, valueLabel) {
                    if (!canvas) return;
                    const ctx = canvas.getContext('2d');
                    const width = canvas.clientWidth || 700;
                    const height = canvas.clientHeight || 300;
                    canvas.width = width;
                    canvas.height = height;
                    ctx.clearRect(0, 0, width, height);
                    ctx.fillStyle = '#f8fbff';
                    ctx.fillRect(0, 0, width, height);

                    if (!matrix || !matrix.length || !matrix[0] || !matrix[0].length) {
                        ctx.fillStyle = '#607087';
                        ctx.font = '12px ui-monospace, SFMono-Regular, Menlo, Consolas, monospace';
                        ctx.fillText('No heatmap data', 12, 20);
                        return;
                    }

                    const rows = matrix.length;
                    const cols = matrix[0].length;
                    let min = Number.POSITIVE_INFINITY;
                    let max = Number.NEGATIVE_INFINITY;
                    for (let r = 0; r < rows; r++) {
                        for (let c = 0; c < cols; c++) {
                            const v = Number(matrix[r][c] || 0);
                            min = Math.min(min, v);
                            max = Math.max(max, v);
                        }
                    }
                    const span = Math.max(1e-12, max - min);
                    const padLeft = 46;
                    const padBottom = 22;
                    const padTop = 10;
                    const plotW = Math.max(10, width - padLeft - 10);
                    const plotH = Math.max(10, height - padTop - padBottom);
                    const cellW = plotW / cols;
                    const cellH = plotH / rows;

                    for (let r = 0; r < rows; r++) {
                        for (let c = 0; c < cols; c++) {
                            const v = Number(matrix[r][c] || 0);
                            const t = (v - min) / span;
                            ctx.fillStyle = heatColor(t);
                            ctx.fillRect(padLeft + c * cellW, padTop + r * cellH, Math.ceil(cellW), Math.ceil(cellH));
                        }
                    }
                    ctx.strokeStyle = '#9db1c7';
                    ctx.lineWidth = 1;
                    ctx.strokeRect(padLeft, padTop, plotW, plotH);

                    ctx.fillStyle = '#4a5c73';
                    ctx.font = '11px ui-monospace, SFMono-Regular, Menlo, Consolas, monospace';
                    ctx.fillText(yLabel, 6, padTop + 10);
                    const xLabelWidth = ctx.measureText(xLabel).width;
                    ctx.fillText(xLabel, width - 12 - xLabelWidth, height - 6);
                    ctx.fillText(`${valueLabel}: ${min.toFixed(3)}..${max.toFixed(3)}`, 6, height - 6);
                }

                function safe(value, fallback) {
                    return value === null || value === undefined ? fallback : value;
                }

                if (representation === 'binary') {
                    const entropySeries = latentSeries.map(l => ((l && l.insights && l.insights.entropyPerBit) || []).map(Number));
                    drawHeatmap(document.getElementById('binaryEntropyHeatmapReport'), transpose(entropySeries), 'generation', 'bit', 'entropy');

                    const fixation = latentSeries.map(l => Number((l && l.metrics && l.metrics.binary_fixation_ratio) || 0));
                    new Chart(document.getElementById('binaryFixationReport'), {
                        type: 'line',
                        data: { labels: iterations, datasets: [{ label: 'Fixation ratio', data: fixation, borderColor: '#0f766e', tension: 0.2 }] },
                        options: { responsive: true, maintainAspectRatio: false }
                    });

                    const probSeries = latentSeries.map(l => ((l && l.insights && l.insights.probabilities) || []).map(Number));
                    const topBits = topChangingIndexes(probSeries, 6);
                    new Chart(document.getElementById('binaryProbReport'), {
                        type: 'line',
                        data: {
                            labels: iterations,
                            datasets: topBits.map((bit, idx) => ({
                                label: `p[${bit}]`,
                                data: latentSeries.map(l => Number((((l && l.insights && l.insights.probabilities) || [])[bit]) || 0)),
                                borderColor: palette(idx),
                                tension: 0.2
                            }))
                        },
                        options: { responsive: true, maintainAspectRatio: false }
                    });

                    const latest = latentSeries.length ? latentSeries[latentSeries.length - 1] : null;
                    const edges = latest && latest.insights ? (latest.insights.dependencyEdges || []) : [];
                    const body = document.getElementById('binaryDepsReport');
                    if (body) {
                        body.innerHTML = edges.length
                            ? edges.map(edge => `<tr><td>${safe(edge.i,'')}</td><td>${safe(edge.j,'')}</td><td>${Number(edge.weight || 0).toFixed(6)}</td><td>${Number(edge.correlation || 0).toFixed(6)}</td></tr>`).join('')
                            : '<tr><td colspan=\"4\">No dependency edges.</td></tr>';
                    }
                }

                if (representation === 'permutation') {
                    const latest = latentSeries.length ? latentSeries[latentSeries.length - 1] : null;
                    const matrix = latest && latest.insights ? (latest.insights.positionDistribution || []) : [];
                    drawHeatmap(document.getElementById('permHeatmapReport'), matrix, 'position', 'item', 'prob');

                    const consensusDrift = latentSeries.map(l => Number((l && l.drift && l.drift.consensus_kendall) || 0));
                    new Chart(document.getElementById('permConsensusReport'), {
                        type: 'line',
                        data: { labels: iterations, datasets: [{ label: 'Consensus Kendall drift', data: consensusDrift, borderColor: '#0b4a6f', tension: 0.2 }] },
                        options: { responsive: true, maintainAspectRatio: false }
                    });

                    const edges = latest && latest.insights ? (latest.insights.topAdjacencyEdges || []) : [];
                    const body = document.getElementById('permEdgesReport');
                    if (body) {
                        body.innerHTML = edges.length
                            ? edges.map(edge => `<tr><td>${safe(edge.from,'')}</td><td>${safe(edge.to,'')}</td><td>${Number(edge.frequency || 0).toFixed(6)}</td><td>${Number(edge.trend || 0).toFixed(6)}</td></tr>`).join('')
                            : '<tr><td colspan=\"4\">No adjacency edges.</td></tr>';
                    }
                }

                if (representation === 'real') {
                    const sigmaSeries = latentSeries.map(l => ((l && l.insights && l.insights.sigmaVector) || []).map(Number));
                    drawHeatmap(document.getElementById('realSigmaReport'), transpose(sigmaSeries), 'generation', 'dimension', 'sigma');

                    const meanSeries = latentSeries.map(l => ((l && l.insights && l.insights.meanVector) || []).map(Number));
                    const topDims = topChangingIndexes(meanSeries, 6);
                    new Chart(document.getElementById('realMeanReport'), {
                        type: 'line',
                        data: {
                            labels: iterations,
                            datasets: topDims.map((dim, idx) => ({
                                label: `mu[${dim}]`,
                                data: latentSeries.map(l => Number((((l && l.insights && l.insights.meanVector) || [])[dim]) || 0)),
                                borderColor: palette(idx),
                                tension: 0.2
                            }))
                        },
                        options: { responsive: true, maintainAspectRatio: false }
                    });

                    const latest = latentSeries.length ? latentSeries[latentSeries.length - 1] : null;
                    const eigen = latest && latest.insights ? (latest.insights.eigenvalues || []) : [];
                    const body = document.getElementById('realEigenReport');
                    if (body) {
                        body.innerHTML = eigen.length
                            ? eigen.map((value, idx) => `<tr><td>${idx + 1}</td><td>${Number(value).toFixed(6)}</td></tr>`).join('')
                            : '<tr><td colspan=\"2\">No eigenvalues.</td></tr>';
                    }
                }
                </script>
                </body>
                </html>
                """.formatted(
                escapeHtml(String.valueOf(summary.get("runId"))),
                escapeHtml(String.valueOf(summary.get("status"))),
                escapeHtml(String.valueOf(summary.get("algorithm"))),
                escapeHtml(String.valueOf(summary.get("model"))),
                escapeHtml(String.valueOf(summary.get("problem"))),
                escapeHtml(String.valueOf(summary.get("bestFitness"))),
                escapeHtml(String.valueOf(summary.get("iterations"))),
                escapeHtml(String.valueOf(summary.get("evaluations"))),
                escapeHtml(String.valueOf(summary.get("runtimeMillis"))),
                escapeHtml(representation),
                escapeHtml(highlightsJson),
                eventsRows,
                representationInsightsSection,
                iterationSeries,
                bestSeries,
                meanSeries,
                stdSeries,
                diversitySeries,
                driftSeries,
                latentSeriesJson,
                mapper.writeValueAsString(representation)
        );
    }

    private String representationInsightsSection(String representationFamily) {
        String family = representationFamily == null ? "unknown" : representationFamily.toLowerCase(Locale.ROOT);
        return switch (family) {
            case "binary" -> """
                    <section class=\"card\">
                        <h2>Binary Latent Insights</h2>
                        <div class=\"row\">
                            <div>
                                <h3>Entropy Heatmap</h3>
                                <div class=\"chart\"><canvas id=\"binaryEntropyHeatmapReport\"></canvas></div>
                            </div>
                            <div>
                                <h3>Top Changing Probabilities</h3>
                                <div class=\"chart\"><canvas id=\"binaryProbReport\"></canvas></div>
                            </div>
                        </div>
                        <div class=\"row\" style=\"margin-top:12px;\">
                            <div>
                                <h3>Fixation Curve</h3>
                                <div class=\"chart\"><canvas id=\"binaryFixationReport\"></canvas></div>
                            </div>
                            <div>
                                <h3>Top Dependencies</h3>
                                <table>
                                    <thead><tr><th>i</th><th>j</th><th>Weight</th><th>Correlation</th></tr></thead>
                                    <tbody id=\"binaryDepsReport\"></tbody>
                                </table>
                            </div>
                        </div>
                    </section>
                    """;
            case "permutation" -> """
                    <section class=\"card\">
                        <h2>Permutation Latent Insights</h2>
                        <div class=\"row\">
                            <div>
                                <h3>Item-Position Heatmap</h3>
                                <div class=\"chart\"><canvas id=\"permHeatmapReport\"></canvas></div>
                            </div>
                            <div>
                                <h3>Consensus Drift</h3>
                                <div class=\"chart\"><canvas id=\"permConsensusReport\"></canvas></div>
                            </div>
                        </div>
                        <h3 style=\"margin-top:12px;\">Top Adjacency Edges</h3>
                        <table>
                            <thead><tr><th>From</th><th>To</th><th>Frequency</th><th>Trend</th></tr></thead>
                            <tbody id=\"permEdgesReport\"></tbody>
                        </table>
                    </section>
                    """;
            case "real" -> """
                    <section class=\"card\">
                        <h2>Real-Valued Latent Insights</h2>
                        <div class=\"row\">
                            <div>
                                <h3>Sigma Heatmap</h3>
                                <div class=\"chart\"><canvas id=\"realSigmaReport\"></canvas></div>
                            </div>
                            <div>
                                <h3>Top Mean Trajectories</h3>
                                <div class=\"chart\"><canvas id=\"realMeanReport\"></canvas></div>
                            </div>
                        </div>
                        <h3 style=\"margin-top:12px;\">Eigen Spectrum (latest)</h3>
                        <table>
                            <thead><tr><th>#</th><th>Eigenvalue</th></tr></thead>
                            <tbody id=\"realEigenReport\"></tbody>
                        </table>
                    </section>
                    """;
            default -> """
                    <section class=\"card\">
                        <h2>Representation-Specific Insights</h2>
                        <p>No dedicated renderer for representation family: <strong>%s</strong></p>
                    </section>
                    """.formatted(escapeHtml(family));
        };
    }

    private static double diversitySignal(String family, LatentTelemetry telemetry) {
        if (telemetry == null) {
            return 0.0;
        }
        return switch (family.toLowerCase(Locale.ROOT)) {
            case "binary" -> telemetry.diversity().getOrDefault("hamming_population", 0.0);
            case "permutation" -> telemetry.diversity().getOrDefault("kendall_population", 0.0);
            case "real" -> telemetry.diversity().getOrDefault("euclidean_population", 0.0);
            default -> 0.0;
        };
    }

    private static double driftSignal(String family, LatentTelemetry telemetry) {
        if (telemetry == null) {
            return 0.0;
        }
        return switch (family.toLowerCase(Locale.ROOT)) {
            case "binary" -> telemetry.drift().getOrDefault("binary_prob_l2", 0.0);
            case "permutation" -> telemetry.drift().getOrDefault("consensus_kendall", 0.0);
            case "real" -> telemetry.drift().getOrDefault("gaussian_kl_diag", 0.0);
            default -> 0.0;
        };
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Compact iteration row persisted for local report generation.
     */
    private record IterationSnapshot(
            String timestamp,
            int iteration,
            long evaluations,
            int populationSize,
            int eliteSize,
            double bestFitness,
            double meanFitness,
            double stdFitness,
            String representationFamily,
            double diversitySignal,
            double driftSignal,
            Map<String, Double> metrics,
            LatentTelemetry latentTelemetry,
            List<AdaptiveActionRecord> adaptiveActions
    ) {
    }
}
